package com.example.demo.services.queries;

import com.example.demo.commons.enums.InventoryLogType;
import com.example.demo.configs.SecurityUtils;
import com.example.demo.dtos.mappers.inventory_log.InventoryLogMapper;
import com.example.demo.dtos.responses.inventory.InventoryLogListResponse;
import com.example.demo.dtos.responses.inventory.InventoryLogResponse;
import com.example.demo.dtos.responses.inventory.InventoryStatusResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Inventory;
import com.example.demo.entities.InventoryLog;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.InventoryLogQueryRepository;
import com.example.demo.repositories.queries.InventoryQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryLogQueryService {

    private final InventoryLogQueryRepository inventoryLogQueryRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final InventoryLogMapper inventoryLogMapper;
    private final SecurityUtils securityUtils;

    /**
     * API: GET /seller/inventory/logs
     * Get inventory logs with filters and pagination
     */
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryLogListResponse getAllInventoryLogs(
            Integer page,
            Integer limit,
            Long warehouseId,
            Long productId,
            String typeStr,
            String fromDate,
            String toDate) {

        log.info("📋 Fetching inventory logs - page: {}, limit: {}, warehouseId: {}, productId: {}, type: {}",
                page, limit, warehouseId, productId, typeStr);

        String currentUserEmail = securityUtils.getCurrentUserEmail();
        boolean isAdmin = securityUtils.hasRole("ADMIN");

        // Parse type from string
        InventoryLogType type = parseType(typeStr);

        // Parse dates
        LocalDateTime fromDateTime = parseDate(fromDate, true);
        LocalDateTime toDateTime = parseDate(toDate, false);

        // Create pageable
        Pageable pageable = PageRequest.of(
                page - 1,
                Math.min(limit, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<InventoryLog> logPage;

        // Admin sees all, Seller sees only their products
        if (isAdmin) {
            logPage = inventoryLogQueryRepository.findAllWithFilters(
                    warehouseId, productId, type, fromDateTime, toDateTime, pageable);
            log.info("📊 Fetching all inventory logs for admin");
        } else {
            // Seller only sees logs for their products
            Account currentAccount = accountQueryRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            logPage = inventoryLogQueryRepository.findAllByCreatedByWithFilters(
                    currentAccount, warehouseId, productId, type, fromDateTime, toDateTime, pageable);
            log.info("📊 Fetching inventory logs for seller: {}", currentUserEmail);
        }

        // Map to response
        List<InventoryLogResponse> logs = logPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Build pagination
        InventoryLogListResponse.PaginationInfo pagination =
                InventoryLogListResponse.PaginationInfo.builder()
                        .currentPage(page)
                        .totalPages(logPage.getTotalPages())
                        .totalItems(logPage.getTotalElements())
                        .perPage(limit)
                        .build();

        return InventoryLogListResponse.builder()
                .logs(logs)
                .pagination(pagination)
                .build();
    }

    /**
     * API: GET /seller/inventory/logs/{id}
     * Get single inventory log detail
     */
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryLogResponse getInventoryLogById(Long id) {
        log.info("🔍 Fetching inventory log with id: {}", id);

        InventoryLog inventoryLog = inventoryLogQueryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory log not found with id: " + id));

        // Check permission
        if (!securityUtils.hasRole("ADMIN")) {
            if (inventoryLog.getProduct().getCreatedBy() == null ||
                    !securityUtils.isOwner(inventoryLog.getProduct().getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to view this inventory log");
            }
        }

        return mapToResponse(inventoryLog);
    }

    /**
     * Get inventory status for a product in a warehouse
     */
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public InventoryStatusResponse getInventoryStatus(Long productId, Long warehouseId) {
        log.info("📦 Fetching inventory status for product: {} in warehouse: {}", productId, warehouseId);

        Inventory inventory = inventoryQueryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + " in warehouse " + warehouseId));

        // Check permission
        if (!securityUtils.hasRole("ADMIN")) {
            if (inventory.getProduct().getCreatedBy() == null ||
                    !securityUtils.isOwner(inventory.getProduct().getCreatedBy().getEmail())) {
                throw new ForbiddenException("You don't have permission to view this inventory");
            }
        }

        return InventoryStatusResponse.builder()
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .warehouseId(inventory.getWarehouse().getId())
                .warehouseName(inventory.getWarehouse().getName())
                .quantityOnHand(inventory.getQuantityOnHand())
                .safetyStock(inventory.getSafetyStock())
                .isLowStock(inventory.isLowStock())
                .lastUpdatedAt(inventory.getLastUpdatedAt())
                .build();
    }

    // ============= Helper Methods =============

    /**
     * Map InventoryLog to Response with calculated quantity before/after
     */
    private InventoryLogResponse mapToResponse(InventoryLog log) {
        // Calculate quantity_before and quantity_after
        Integer quantityChange = calculateQuantityChange(log.getType(), log.getQuantity());

        // We need to get current inventory to calculate the before/after
        // For simplicity, we'll just show the change
        return inventoryLogMapper.toResponse(log, null, quantityChange, null);
    }

    private int calculateQuantityChange(InventoryLogType type, Integer quantity) {
        switch (type) {
            case IN:
            case RETURN:
            case TRANSFER_IN:
                return quantity;
            case OUT:
            case TRANSFER_OUT:
                return -quantity;
            case ADJUST:
                return quantity;
            default:
                return 0;
        }
    }

    /**
     * Parse type string to enum
     * Supports: "in", "out", "adjust", "return", "transfer"
     */
    private InventoryLogType parseType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return null;
        }

        try {
            // Try to parse as number first
            int code = Integer.parseInt(typeStr);
            return InventoryLogType.fromCode(code);
        } catch (NumberFormatException e) {
            // Parse as string
            return InventoryLogType.fromString(typeStr.toUpperCase());
        }
    }

    /**
     * Parse date string to LocalDateTime
     * Format: YYYY-MM-DD
     */
    private LocalDateTime parseDate(String dateStr, boolean startOfDay) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return startOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }
}