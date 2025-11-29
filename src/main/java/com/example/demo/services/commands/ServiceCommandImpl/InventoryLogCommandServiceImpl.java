package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.InventoryLogType;
import com.example.demo.services.commands.InventoryLogCommandService;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.commands.inventory.CreateInventoryLogRequest;
import com.example.demo.dtos.mappers.inventory_log.InventoryLogMapper;
import com.example.demo.dtos.responses.inventory.InventoryLogResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryLogCommandServiceImpl implements InventoryLogCommandService {

    private final InventoryLogCommandRepository inventoryLogCommandRepository;
    private final InventoryCommandRepository inventoryCommandRepository;
    private final ProductCommandRepository productCommandRepository;
    private final WarehouseCommandRepository warehouseCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final InventoryLogMapper inventoryLogMapper;
    private final SecurityUtils securityUtils;

    /**
     * Transaction-safe with PESSIMISTIC_WRITE lock to avoid race conditions
     **/
    @Transactional(transactionManager = "writeTransactionManager")
    public InventoryLogResponse createInventoryLog(CreateInventoryLogRequest request) {
        log.info("🔄 Creating inventory log - Type: {}, ProductId: {}, WarehouseId: {}, Quantity: {}",
                request.getType(), request.getProductId(), request.getWarehouseId(), request.getQuantity());

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();
        User currentUser = accountCommandRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")).getUser();

        // Validate product exists
        Product product = productCommandRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // Validate warehouse exists
        Warehouse warehouse = warehouseCommandRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));

        // Check permissions: Seller can only manage their own products
        if (!securityUtils.hasRole("ADMIN") && product.getCreatedBy() != null) {
            if (!securityUtils.isOwner(product.getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to manage inventory for this product");
            }
        }

        // Get or create inventory with PESSIMISTIC LOCK to prevent race conditions
        Inventory inventory = inventoryCommandRepository
                .findByProductAndWarehouseWithLock(product, warehouse)
                .orElseGet(() -> {
                    log.info("📦 Creating new inventory record for product {} in warehouse {}",
                            product.getId(), warehouse.getId());
                    Inventory newInventory = Inventory.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantityOnHand(0)
                            .safetyStock(10) // Default safety stock
                            .lastUpdatedAt(LocalDateTime.now())
                            .build();
                    return inventoryCommandRepository.save(newInventory);
                });

        InventoryLogType typeEnum = request.getTypeEnum();

        // Validate
        validateInventoryOperation(inventory, typeEnum, request.getQuantity());

        // Update inventory quantity
        int oldQuantity = inventory.getQuantityOnHand();
        int newQuantity = calculateNewQuantity(inventory.getQuantityOnHand(), typeEnum, request.getQuantity());

        inventory.setQuantityOnHand(newQuantity);
        inventory.setLastUpdatedAt(LocalDateTime.now());
        inventoryCommandRepository.save(inventory);

        log.info("📊 Updated inventory: {} → {} (Type: {}, Change: {})",
                oldQuantity, newQuantity, request.getType(), request.getQuantity());

        // Create inventory log
        InventoryLog inventoryLog = inventoryLogMapper.toEntity(request);
        inventoryLog.setWarehouse(warehouse);
        inventoryLog.setProduct(product);
        inventoryLog.setUser(currentUser);

        InventoryLog savedLog = inventoryLogCommandRepository.save(inventoryLog);

        log.info("✅ Inventory log created successfully with id: {} by user: {}",
                savedLog.getId(), currentUserEmail);

        return inventoryLogMapper.toResponse(savedLog);
    }

    /**
     * Validate business rules for operation types
     **/
    private void validateInventoryOperation(Inventory inventory, InventoryLogType type, Integer quantity) {
        switch (type) {
            case OUT:
                // Check that no more stock is issued than the available quantity (NO OVERSELL)
                if (inventory.getQuantityOnHand() < quantity) {
                    throw new BadRequestException(
                            String.format("Insufficient inventory. Available: %d, Requested: %d",
                                    inventory.getQuantityOnHand(), quantity)
                    );
                }
                break;

            case IN:
                // No limit for warehousing
                break;

            case ADJUST:
                // The adjustment can be negative or positive, but the result cannot be negative
                int resultQuantity = inventory.getQuantityOnHand() + quantity;
                if (resultQuantity < 0) {
                    throw new BadRequestException(
                            String.format("Invalid adjustment. Current: %d, Adjustment: %d would result in negative inventory",
                                    inventory.getQuantityOnHand(), quantity)
                    );
                }
                break;

            case RETURN:
                // The refund always increases in amount, no limit
                break;

            default:
                throw new BadRequestException("Invalid inventory log type: " + type);
        }
    }

    /**
     * Calculate new quantity based on operation type
     **/
    private int calculateNewQuantity(int currentQuantity, InventoryLogType type, int quantity) {
        switch (type) {
            case IN:
            case RETURN:
                return currentQuantity + quantity;
            case OUT:
                return currentQuantity - quantity;
            case ADJUST:
                // ADJUST can be negative or positive (validated above)
                return currentQuantity + quantity;
            default:
                throw new BadRequestException("Invalid inventory log type: " + type);
        }
    }

    /**
     * Delete log (soft delete)
     * Only admin can delete log
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteInventoryLog(Long id) {
        log.info("🗑️ Deleting inventory log with id: {}", id);

        // Only admin can delete logs
        if (!securityUtils.hasRole("ADMIN")) {
            throw new ForbiddenException("Only administrators can delete inventory logs");
        }

        InventoryLog inventoryLog  = inventoryLogCommandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory log not found with id: " + id));

        inventoryLog .softDelete();
        inventoryLogCommandRepository.save(inventoryLog );

        log.info("✅ Inventory log soft deleted successfully with id: {}", id);
    }
}
