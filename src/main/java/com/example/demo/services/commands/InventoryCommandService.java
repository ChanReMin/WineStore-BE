package com.example.demo.services.commands;

import com.example.demo.commons.enums.InventoryLogType;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.commands.inventory.StockTakeRequest;
import com.example.demo.dtos.commands.inventory.TransferInventoryRequest;
import com.example.demo.dtos.commands.inventory.UpdateInventoryRequest;
import com.example.demo.dtos.responses.inventory.StockTakeResponse;
import com.example.demo.dtos.responses.inventory.TransferInventoryResponse;
import com.example.demo.dtos.responses.inventory.UpdateInventoryResponse;
import com.example.demo.entities.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryCommandService {

    private final InventoryCommandRepository inventoryCommandRepository;
    private final InventoryLogCommandRepository inventoryLogCommandRepository;
    private final ProductCommandRepository productCommandRepository;
    private final WarehouseCommandRepository warehouseCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final SecurityUtils securityUtils;

    /**
     * Update inventory quantity (IN/OUT/ADJUST)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateInventoryResponse updateInventory(Long inventoryId, UpdateInventoryRequest request) {
        log.info("📦 Updating inventory {} - Type: {}, Quantity: {}",
                inventoryId, request.getType(), request.getQuantity());

        // Get current user
        Account currentAccount = accountCommandRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get inventory with lock
        Inventory inventory = inventoryCommandRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        // Check permission
        if (!securityUtils.hasRole("ADMIN")) {
            if (inventory.getProduct().getCreatedBy() == null ||
                    !securityUtils.isOwner(inventory.getProduct().getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to update this inventory");
            }
        }

        // Parse type
        InventoryLogType type = parseInventoryLogType(request.getType());

        // Store old quantity
        int oldQuantity = inventory.getQuantityOnHand();

        // Validate and calculate new quantity
        validateInventoryUpdate(inventory, type, request.getQuantity());
        int quantityChange = calculateQuantityChange(type, request.getQuantity());
        int newQuantity = oldQuantity + quantityChange;

        // Update inventory
        inventory.setQuantityOnHand(newQuantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());
        inventoryCommandRepository.save(inventory);

        // Create inventory log
        InventoryLog logg = InventoryLog.builder()
                .warehouse(inventory.getWarehouse())
                .product(inventory.getProduct())
                .user(currentAccount.getUser())
                .type(type)
                .quantity(request.getQuantity())
                .note(request.getNote())
                .build();
        InventoryLog savedLog = inventoryLogCommandRepository.save(logg);

        log.info("✅ Inventory updated: {} → {} (Change: {})", oldQuantity, newQuantity, quantityChange);

        return UpdateInventoryResponse.builder()
                .inventoryId(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .productId(inventory.getProduct().getId())
                .oldQuantity(oldQuantity)
                .newQuantity(newQuantity)
                .quantityChange(quantityChange)
                .type(request.getType())
                .logId(savedLog.getId())
                .updatedAt(inventory.getLastUpdatedAt())
                .build();
    }

    /**
     * API 4: POST /seller/inventory/transfer
     * Transfer inventory between warehouses
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public TransferInventoryResponse transferInventory(TransferInventoryRequest request) {
        log.info("🚚 Transferring inventory - ProductId: {}, From: {}, To: {}, Qty: {}",
                request.getProductId(), request.getFromWarehouseId(),
                request.getToWarehouseId(), request.getQuantity());

        // Validate
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new BadRequestException("Cannot transfer to the same warehouse");
        }

        // Get current user
        Account currentAccount = accountCommandRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate product and warehouses
        Product product = productCommandRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Warehouse fromWarehouse = warehouseCommandRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("From warehouse not found"));

        Warehouse toWarehouse = warehouseCommandRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("To warehouse not found"));

        // Check permission
        if (!securityUtils.hasRole("ADMIN") && product.getCreatedBy() != null) {
            if (!securityUtils.isOwner(product.getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to transfer this product");
            }
        }

        // Get inventory with lock
        Inventory fromInventory = inventoryCommandRepository
                .findByProductAndWarehouseWithLock(product, fromWarehouse)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found in source warehouse"));

        // Validate quantity
        if (fromInventory.getQuantityOnHand() < request.getQuantity()) {
            throw new BadRequestException(
                    String.format("Insufficient inventory. Available: %d, Requested: %d",
                            fromInventory.getQuantityOnHand(), request.getQuantity()));
        }

        // Create TRANSFER_OUT log
        InventoryLog outLog = InventoryLog.builder()
                .warehouse(fromWarehouse)
                .product(product)
                .user(currentAccount.getUser())
                .type(InventoryLogType.TRANSFER_OUT)
                .quantity(request.getQuantity())
                .note(request.getNote())
                .build();
        inventoryLogCommandRepository.save(outLog);

        // Update from inventory
        fromInventory.setQuantityOnHand(fromInventory.getQuantityOnHand() - request.getQuantity());
        fromInventory.setLastUpdatedAt(LocalDateTime.now());
        inventoryCommandRepository.save(fromInventory);

        // Get or create to inventory
        Inventory toInventory = inventoryCommandRepository
                .findByProductAndWarehouse(product, toWarehouse)
                .orElseGet(() -> {
                    Inventory newInv = Inventory.builder()
                            .product(product)
                            .warehouse(toWarehouse)
                            .quantityOnHand(0)
                            .safetyStock(10)
                            .lastUpdatedAt(LocalDateTime.now())
                            .build();
                    return inventoryCommandRepository.save(newInv);
                });

        // Create TRANSFER_IN log
        InventoryLog inLog = InventoryLog.builder()
                .warehouse(toWarehouse)
                .product(product)
                .user(currentAccount.getUser())
                .type(InventoryLogType.TRANSFER_IN)
                .quantity(request.getQuantity())
                .note(request.getNote())
                .shipmentId(outLog.getId())
                .build();
        inventoryLogCommandRepository.save(inLog);

        // Update to inventory
        toInventory.setQuantityOnHand(toInventory.getQuantityOnHand() + request.getQuantity());
        toInventory.setLastUpdatedAt(LocalDateTime.now());
        inventoryCommandRepository.save(toInventory);

        log.info("✅ Transfer completed successfully");

        // Generate transfer code
        String transferCode = generateTransferCode(outLog.getId());

        return TransferInventoryResponse.builder()
                .transferId(outLog.getId())
                .transferCode(transferCode)
                .productId(product.getId())
                .fromWarehouseId(fromWarehouse.getId())
                .toWarehouseId(toWarehouse.getId())
                .quantity(request.getQuantity())
                .status("completed")
                .statusText("Hoàn thành")
                .createdAt(outLog.getCreatedAt())
                .build();
    }

    /**
     * API 9: POST /seller/inventory/stock-take
     * Perform stock take and adjust inventory
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public StockTakeResponse stockTake(StockTakeRequest request) {
        log.info("📋 Performing stock take for warehouse: {}", request.getWarehouseId());

        Account currentAccount = accountCommandRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Warehouse warehouse = warehouseCommandRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        List<StockTakeResponse.AdjustmentInfo> adjustments = new ArrayList<>();
        int itemsAdjusted = 0;

        for (StockTakeRequest.StockTakeItem item : request.getItems()) {
            Product product = productCommandRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));

            // Get inventory with lock
            Inventory inventory = inventoryCommandRepository
                    .findByProductAndWarehouseWithLock(product, warehouse)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

            // Calculate difference
            int difference = item.getActualQuantity() - item.getSystemQuantity();

            if (difference != 0) {
                // Create ADJUST log
                InventoryLog log = InventoryLog.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .user(currentAccount.getUser())
                        .type(InventoryLogType.ADJUST)
                        .quantity(Math.abs(difference))
                        .note(item.getNote() != null ? item.getNote() : "Stock take adjustment")
                        .build();
                inventoryLogCommandRepository.save(log);

                // Update inventory
                inventory.setQuantityOnHand(item.getActualQuantity());
                inventory.setLastUpdatedAt(LocalDateTime.now());
                inventoryCommandRepository.save(inventory);

                // Add to adjustments
                adjustments.add(StockTakeResponse.AdjustmentInfo.builder()
                        .productId(product.getId())
                        .difference(difference)
                        .note(item.getNote())
                        .build());

                itemsAdjusted++;
            }
        }

        log.info("✅ Stock take completed. Items adjusted: {}", itemsAdjusted);

        return StockTakeResponse.builder()
                .stockTakeId(System.currentTimeMillis())
                .warehouseId(warehouse.getId())
                .totalItems(request.getItems().size())
                .itemsAdjusted(itemsAdjusted)
                .adjustments(adjustments)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String generateTransferCode(Long logId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TRF-%s-%04d", date, logId);
    }

    private InventoryLogType parseInventoryLogType(String type) {
        return switch (type.toLowerCase()) {
            case "in" -> InventoryLogType.IN;
            case "out" -> InventoryLogType.OUT;
            case "adjust" -> InventoryLogType.ADJUST;
            case "return" -> InventoryLogType.RETURN;
            default -> throw new BadRequestException("Invalid inventory type: " + type);
        };
    }

    private void validateInventoryUpdate(Inventory inventory, InventoryLogType type, Integer quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        if (type == InventoryLogType.OUT) {
            if (inventory.getQuantityOnHand() < quantity) {
                throw new BadRequestException(
                        String.format("The quantity issued exceeds the current inventory. Available: %d, Requested: %d",
                                inventory.getQuantityOnHand(), quantity)
                );
            }
        }

        if (type == InventoryLogType.ADJUST) {
            int resultQuantity = inventory.getQuantityOnHand() + quantity;
            if (resultQuantity < 0) {
                throw new BadRequestException(
                        String.format("Invalid adjustment. Current: %d, Adjustment: %d would result in negative inventory",
                                inventory.getQuantityOnHand(), quantity)
                );
            }
        }
    }

    private int calculateQuantityChange(InventoryLogType type, int quantity) {
        return switch (type) {
            case IN, RETURN, TRANSFER_IN -> quantity;
            case OUT, TRANSFER_OUT -> -quantity;
            case ADJUST -> quantity;
        };
    }
}