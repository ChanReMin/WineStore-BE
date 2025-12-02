package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.commons.enums.InventoryStatus;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.repositories.queries.*;
import com.example.demo.services.queries.InventoryQueryService;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.responses.inventory.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.Inventory;
import com.example.demo.entities.InventoryLog;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryQueryServiceImpl implements InventoryQueryService {

    private final InventoryQueryRepository inventoryQueryRepository;
    private final InventoryLogQueryRepository inventoryLogQueryRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final WarehouseQueryRepository warehouseQueryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryListResponse getAllInventory(
            Integer page,
            Integer limit,
            Long warehouseId,
            Long productId,
            String status,
            String search) {

        log.info("📦 Fetching inventory - page: {}, limit: {}, warehouseId: {}, status: {}",
                page, limit, warehouseId, status);

        String currentUserEmail = securityUtils.getCurrentUserEmail();
        boolean isAdmin = securityUtils.hasRole("ADMIN");

        if (page < 1 && limit < 1) {
            throw new IllegalArgumentException("Page and limit must be greater than 0");
        }

        if (warehouseId != null) {
            warehouseQueryRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        }

        if (productId != null) {
            productQueryRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        }

        InventoryStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = InventoryStatus.fromCode(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid inventory status value: " + status);
            }
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                Math.min(limit, 100),
                Sort.by(Sort.Direction.DESC, "lastUpdatedAt")
        );

        Page<Inventory> inventoryPage;

        if (isAdmin) {
            inventoryPage = inventoryQueryRepository.findAllWithFilters(
                    warehouseId, productId, statusEnum != null ? statusEnum.getCode() : null, search, pageable);
        } else {
            Account currentAccount = accountQueryRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            inventoryPage = inventoryQueryRepository.findAllByCreatedByWithFilters(
                    currentAccount, warehouseId, productId,
                    statusEnum != null ? statusEnum.getCode() : null, search, pageable);
        }

        List<InventoryItemResponse> inventory = inventoryPage.getContent().stream()
                .map(this::toInventoryItemResponse)
                .collect(Collectors.toList());

        InventoryListResponse.PaginationInfo pagination =
                InventoryListResponse.PaginationInfo.builder()
                        .currentPage(page)
                        .totalPages(inventoryPage.getTotalPages())
                        .totalItems(inventoryPage.getTotalElements())
                        .perPage(limit)
                        .build();

        InventoryListResponse.SummaryInfo summary = buildSummary(currentUserEmail, isAdmin);

        return InventoryListResponse.builder()
                .inventory(inventory)
                .pagination(pagination)
                .summary(summary)
                .build();
    }


    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryDetailResponse getInventoryById(Long inventoryId) {
        log.info("🔍 Fetching inventory with id: {}", inventoryId);

        Inventory inventory = inventoryQueryRepository.findByIdWithDetails(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        // Check permission
        if (!securityUtils.hasRole("ADMIN")) {
            if (inventory.getProduct().getCreatedBy() == null ||
                    !securityUtils.isOwner(inventory.getProduct().getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to view this inventory");
            }
        }

        return toInventoryDetailResponse(inventory);
    }

    /**
     * Get inventory alerts
     */
    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryAlertsResponse getInventoryAlerts(String type, Long warehouseId) {
        log.info("⚠️ Fetching inventory alerts - type: {}, warehouseId: {}", type, warehouseId);

        String currentUserEmail = securityUtils.getCurrentUserEmail();
        boolean isAdmin = securityUtils.hasRole("ADMIN");

        List<Inventory> inventories;

        if (isAdmin) {
            inventories = inventoryQueryRepository.findLowStockInventory(warehouseId);
        } else {
            Account currentAccount = accountQueryRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            inventories = inventoryQueryRepository.findLowStockInventoryByCreatedBy(
                    currentAccount, warehouseId);
        }

        // Filter by type if provided
        List<InventoryAlertsResponse.AlertInfo> alerts = inventories.stream()
                .filter(inv -> matchesAlertType(inv, type))
                .map(this::toAlertInfo)
                .collect(Collectors.toList());

        // Build summary
        int critical = (int) alerts.stream()
                .filter(alert -> "critical".equals(alert.getSeverity()))
                .count();
        int warning = (int) alerts.stream()
                .filter(alert -> "warning".equals(alert.getSeverity()))
                .count();

        InventoryAlertsResponse.AlertSummary summary =
                InventoryAlertsResponse.AlertSummary.builder()
                        .totalAlerts(alerts.size())
                        .critical(critical)
                        .warning(warning)
                        .build();

        return InventoryAlertsResponse.builder()
                .alerts(alerts)
                .summary(summary)
                .build();
    }

    // ============= Helper Methods =============

    private InventoryItemResponse toInventoryItemResponse(Inventory inv) {
        return InventoryItemResponse.builder()
                .id(inv.getId())
                .warehouse(InventoryItemResponse.WarehouseInfo.builder()
                        .id(inv.getWarehouse().getId())
                        .name(inv.getWarehouse().getName())
                        .location(inv.getWarehouse().getLocation())
                        .address(inv.getWarehouse().getCity())
                        .build())
                .product(InventoryItemResponse.ProductInfo.builder()
                        .id(inv.getProduct().getId())
                        .name(inv.getProduct().getName())
                        .sku(inv.getProduct().getSku())
                        .price(inv.getProduct().getPrice())
                        .image(inv.getProduct().getImages())
                        .build())
                .quantityOnHand(inv.getQuantityOnHand())
                .safetyStock(inv.getSafetyStock())
                .quantityReserved(0) // TODO: Calculate from orders
                .quantityAvailable(inv.getQuantityOnHand())
                .status(inv.getStatusCode()) // Use the new method
                .lastUpdatedAt(inv.getLastUpdatedAt())
                .lastUpdatedBy("System") // TODO: Get from last log
                .build();
    }

    private InventoryDetailResponse toInventoryDetailResponse(Inventory inv) {
        // Get last stock in/out
        InventoryLog lastIn = inventoryLogQueryRepository
                .findLastStockIn(inv.getProduct().getId(), inv.getWarehouse().getId())
                .orElse(null);

        InventoryLog lastOut = inventoryLogQueryRepository
                .findLastStockOut(inv.getProduct().getId(), inv.getWarehouse().getId())
                .orElse(null);

        return InventoryDetailResponse.builder()
                .id(inv.getId())
                .warehouse(InventoryDetailResponse.WarehouseDetailInfo.builder()
                        .id(inv.getWarehouse().getId())
                        .name(inv.getWarehouse().getName())
                        .location(inv.getWarehouse().getLocation())
                        .address(inv.getWarehouse().getCity())
                        .manager(inv.getWarehouse().getCreatedBy() != null
                                ? inv.getWarehouse().getCreatedBy().getUser().getFirstName()
                                : null)
                        .phone("0123456789") // TODO: Add to warehouse
                        .build())
                .product(InventoryDetailResponse.ProductDetailInfo.builder()
                        .id(inv.getProduct().getId())
                        .name(inv.getProduct().getName())
                        .sku(inv.getProduct().getSku())
                        .price(inv.getProduct().getPrice())
                        .costPrice(inv.getProduct().getCostPrice())
                        .image(inv.getProduct().getImages())
                        .build())
                .quantityOnHand(inv.getQuantityOnHand())
                .safetyStock(inv.getSafetyStock())
                .quantityReserved(0)
                .quantityAvailable(inv.getQuantityOnHand())
                .quantityIncoming(0) // TODO: Calculate from purchase orders
                .status(inv.getStatusCode()) // Use the new method
                .locationInWarehouse("A-01-03") // TODO: Add to inventory
                .lastStockIn(lastIn != null ? toLastTransactionInfo(lastIn) : null)
                .lastStockOut(lastOut != null ? toLastTransactionInfo(lastOut) : null)
                .lastUpdatedAt(inv.getLastUpdatedAt())
                .lastUpdatedBy("System")
                .build();
    }

    private InventoryDetailResponse.LastTransactionInfo toLastTransactionInfo(InventoryLog log) {
        return InventoryDetailResponse.LastTransactionInfo.builder()
                .date(log.getCreatedAt())
                .quantity(log.getQuantity())
                .note(log.getNote())
                .build();
    }

    /**
     * Check if inventory matches the requested alert type
     */
    private boolean matchesAlertType(Inventory inv, String type) {
        if (type == null || type.isEmpty()) {
            return true;
        }
        return inv.getStatusCode().equals(type);
    }

    private InventoryAlertsResponse.AlertInfo toAlertInfo(Inventory inv) {
        InventoryStatus status = inv.getStatus();
        String statusCode = status.getCode();

        // Severity: critical for out_of_stock, warning for low_stock
        String severity = status == InventoryStatus.OUT_OF_STOCK ? "critical" : "warning";

        // Message based on status
        String message = status == InventoryStatus.OUT_OF_STOCK
                ? "Product out of stock"
                : "Inventory is below safety level";

        return InventoryAlertsResponse.AlertInfo.builder()
                .id(inv.getId())
                .type(statusCode)
                .typeText(status.getDescription())
                .severity(severity)
                .warehouse(InventoryAlertsResponse.AlertInfo.WarehouseInfo.builder()
                        .id(inv.getWarehouse().getId())
                        .name(inv.getWarehouse().getName())
                        .build())
                .product(InventoryAlertsResponse.AlertInfo.ProductInfo.builder()
                        .id(inv.getProduct().getId())
                        .name(inv.getProduct().getName())
                        .sku(inv.getProduct().getSku())
                        .image(inv.getProduct().getImages())
                        .build())
                .currentQuantity(inv.getQuantityOnHand())
                .safetyStock(inv.getSafetyStock())
                .message(message)
                .createdAt(inv.getLastUpdatedAt())
                .build();
    }

    private InventoryListResponse.SummaryInfo buildSummary(String userEmail, boolean isAdmin) {
        Long totalProducts, inStock, lowStock, outOfStock;
        BigDecimal totalValue;

        if (isAdmin) {
            totalProducts = inventoryQueryRepository.countAllActive();
            inStock = inventoryQueryRepository.countByStatus("in_stock");
            lowStock = inventoryQueryRepository.countByStatus("low_stock");
            outOfStock = inventoryQueryRepository.countByStatus("out_of_stock");
            totalValue = inventoryQueryRepository.calculateTotalValue();
        } else {
            Account account = accountQueryRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            totalProducts = inventoryQueryRepository.countByCreatedBy(account);
            inStock = inventoryQueryRepository.countByCreatedByAndStatus(account, "in_stock");
            lowStock = inventoryQueryRepository.countByCreatedByAndStatus(account, "low_stock");
            outOfStock = inventoryQueryRepository.countByCreatedByAndStatus(account, "out_of_stock");
            totalValue = inventoryQueryRepository.calculateTotalValueByCreatedBy(account);
        }

        return InventoryListResponse.SummaryInfo.builder()
                .totalProducts(totalProducts)
                .inStock(inStock)
                .lowStock(lowStock)
                .outOfStock(outOfStock)
                .totalValue(totalValue != null ? totalValue : BigDecimal.ZERO)
                .build();
    }
}