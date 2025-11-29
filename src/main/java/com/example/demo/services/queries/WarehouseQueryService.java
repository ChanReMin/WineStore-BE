package com.example.demo.services.queries;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.mappers.warehouse.WarehouseMapper;
import com.example.demo.dtos.responses.warehouse.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.Warehouse;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WarehouseQueryService {

    private final WarehouseQueryRepository warehouseQueryRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final InventoryLogQueryRepository inventoryLogQueryRepository;
    private final OrderQueryRepository orderQueryRepository; // ✅ Thêm dependency này
    private final SecurityUtils securityUtils;
    private final WarehouseMapper warehouseMapper;

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public Object getWarehouses(Integer status, Long managerId, Integer page,
                                Integer limit, String search, String sortBy, String sortOrder) {

        Account current = getCurrentAccount();
        boolean isSeller = securityUtils.hasRole("SELLER");
        boolean isAdmin = securityUtils.hasRole("ADMIN");

        if (isSeller) {
            managerId = current.getId();
        }

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(direction, sortBy != null ? sortBy : "createdAt")
        );

        Page<Warehouse> warehousePage = warehouseQueryRepository.findAllWithFilters(
                status != null ? ProductStatus.fromCode(status) : null,
                managerId,
                search,
                pageable
        );

        if (isSeller) {
            return buildSellerWarehouseResponse(warehousePage, current.getId());
        } else if (isAdmin) {
            return buildAdminWarehouseResponse(warehousePage);
        }
        throw new ForbiddenException("Role không hợp lệ");
    }

    /**
     * Xem chi tiết warehouse - Chung cho SELLER và ADMIN
     * Response khác nhau tùy theo role
     */
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public WarehouseDetailResponse getWarehouseDetailById(Long warehouseId) {
        log.info("🔍 Fetching warehouse detail: {}", warehouseId);

        Warehouse warehouse = warehouseQueryRepository.findByIdWithDetails(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng"));

        boolean isAdmin = securityUtils.hasRole("ADMIN");

        // Trả về response khác nhau tùy role
        if (isAdmin) {
            return warehouseMapper.toAdminDetailResponse(
                    warehouse,
                    inventoryQueryRepository,
                    inventoryLogQueryRepository,
                    orderQueryRepository // ✅ Truyền thêm orderQueryRepository
            );
        } else {
            return warehouseMapper.toSellerDetailResponse(
                    warehouse,
                    inventoryQueryRepository,
                    inventoryLogQueryRepository
            );
        }
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public WarehouseSellerStatisticsResponse getSellerStatistics() {
        log.info("📊 [SELLER] Fetching statistics");

        Account currentAccount = getCurrentAccount();
        List<Warehouse> warehouses = warehouseQueryRepository.findByManager(currentAccount);

        long totalWarehouses = warehouses.size();
        long activeWarehouses = warehouses.stream().filter(Warehouse::isActive).count();
        long pendingWarehouses = warehouses.stream().filter(Warehouse::isPending).count();
        long bannedWarehouses = warehouses.stream().filter(Warehouse::isBanned).count();

        // Calculate inventory totals
        Integer totalProducts = 0;
        Integer totalQuantity = 0;
        Long totalInventoryValue = 0L;

        for (Warehouse w : warehouses) {
            totalProducts += inventoryQueryRepository.countProductsInWarehouse(w.getId());
            totalQuantity += inventoryQueryRepository.countTotalInventory(w.getId());
            BigDecimal value = inventoryQueryRepository.calculateInventoryValue(w.getId());
            totalInventoryValue += value != null ? value.longValue() : 0L;
        }

        // Build warehouses by status
        List<WarehouseSellerStatisticsResponse.StatusCount> warehousesByStatus = List.of(
                WarehouseSellerStatisticsResponse.StatusCount.builder()
                        .status(1)
                        .count(activeWarehouses)
                        .label("Đang hoạt động")
                        .build(),
                WarehouseSellerStatisticsResponse.StatusCount.builder()
                        .status(0)
                        .count(pendingWarehouses)
                        .label("Chờ duyệt")
                        .build(),
                WarehouseSellerStatisticsResponse.StatusCount.builder()
                        .status(2)
                        .count(bannedWarehouses)
                        .label("Bị khóa")
                        .build()
        );

        return WarehouseSellerStatisticsResponse.builder()
                .totalWarehouses(totalWarehouses)
                .activeWarehouses(activeWarehouses)
                .pendingWarehouses(pendingWarehouses)
                .bannedWarehouses(bannedWarehouses)
                .totalInventoryValue(totalInventoryValue)
                .totalProducts(totalProducts)
                .totalQuantity(totalQuantity)
                .warehousesByStatus(warehousesByStatus)
                .build();
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public WarehouseAdminStatisticsResponse getAdminStatistics(
            Long managerId, LocalDate fromDate, LocalDate toDate) {

        log.info("📊 [ADMIN] Fetching statistics - managerId: {}, from: {}, to: {}",
                managerId, fromDate, toDate);

        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : null;

        // Get warehouses based on filters
        List<Warehouse> warehouses;
        if (managerId != null) {
            Account manager = accountQueryRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            warehouses = warehouseQueryRepository.findByManager(manager);
        } else {
            warehouses = warehouseQueryRepository.findAllActive();
        }

        // Filter by date range
        if (from != null || to != null) {
            warehouses = warehouses.stream()
                    .filter(w -> {
                        if (from != null && w.getCreatedAt().isBefore(from)) return false;
                        if (to != null && w.getCreatedAt().isAfter(to)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        // Calculate overview
        long totalWarehouses = warehouses.size();
        long activeWarehouses = warehouses.stream().filter(Warehouse::isActive).count();
        long pendingWarehouses = warehouses.stream().filter(Warehouse::isPending).count();
        long bannedWarehouses = warehouses.stream().filter(Warehouse::isBanned).count();

        // Calculate inventory totals
        Integer totalProducts = 0;
        Integer totalQuantity = 0;
        Long totalInventoryValue = 0L;

        for (Warehouse w : warehouses) {
            totalProducts += inventoryQueryRepository.countProductsInWarehouse(w.getId());
            totalQuantity += inventoryQueryRepository.countTotalInventory(w.getId());
            BigDecimal value = inventoryQueryRepository.calculateInventoryValue(w.getId());
            totalInventoryValue += value != null ? value.longValue() : 0L;
        }

        // Build overview
        WarehouseAdminStatisticsResponse.Overview overview =
                WarehouseAdminStatisticsResponse.Overview.builder()
                        .totalWarehouses(totalWarehouses)
                        .activeWarehouses(activeWarehouses)
                        .pendingWarehouses(pendingWarehouses)
                        .bannedWarehouses(bannedWarehouses)
                        .build();

        // Build inventory
        WarehouseAdminStatisticsResponse.Inventory inventory =
                WarehouseAdminStatisticsResponse.Inventory.builder()
                        .totalProducts(totalProducts)
                        .totalQuantity(totalQuantity)
                        .totalInventoryValue(totalInventoryValue)
                        .build();

        // Build status breakdown
        List<WarehouseAdminStatisticsResponse.StatusBreakdown> byStatus =
                buildAdminStatusBreakdown(totalWarehouses, activeWarehouses,
                        pendingWarehouses, bannedWarehouses);

        // Build top warehouses (top 5 by inventory value)
        List<WarehouseAdminStatisticsResponse.TopWarehouse> topWarehouses = warehouses.stream()
                .filter(Warehouse::isActive)
                .map(w -> {
                    BigDecimal value = inventoryQueryRepository.calculateInventoryValue(w.getId());
                    Integer products = inventoryQueryRepository.countProductsInWarehouse(w.getId());

                    String managerName = "";
                    if (w.getCreatedBy() != null && w.getCreatedBy().getUser() != null) {
                        managerName = w.getCreatedBy().getUser().getFirstName() +
                                " " + w.getCreatedBy().getUser().getLastName();
                    }

                    return WarehouseAdminStatisticsResponse.TopWarehouse.builder()
                            .id(w.getId())
                            .name(w.getName())
                            .managerName(managerName)
                            .totalValue(value != null ? value.longValue() : 0L)
                            .totalProducts(products)
                            .build();
                })
                .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                .limit(5)
                .collect(Collectors.toList());

        // Build recent requests (last 5 pending warehouses)
        List<WarehouseAdminStatisticsResponse.RecentRequest> recentRequests = warehouses.stream()
                .filter(Warehouse::isPending)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(w -> {
                    String managerName = "";
                    if (w.getCreatedBy() != null && w.getCreatedBy().getUser() != null) {
                        managerName = w.getCreatedBy().getUser().getFirstName() +
                                " " + w.getCreatedBy().getUser().getLastName();
                    }

                    return WarehouseAdminStatisticsResponse.RecentRequest.builder()
                            .id(w.getId())
                            .name(w.getName())
                            .managerName(managerName)
                            .status(w.getStatus().getCode())
                            .createdAt(w.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return WarehouseAdminStatisticsResponse.builder()
                .overview(overview)
                .inventory(inventory)
                .byStatus(byStatus)
                .topWarehouses(topWarehouses)
                .recentRequests(recentRequests)
                .build();
    }

    // ============= Helper Methods =============

    private List<WarehouseAdminStatisticsResponse.StatusBreakdown> buildAdminStatusBreakdown(
            long total, long active, long pending, long banned) {

        return List.of(
                WarehouseAdminStatisticsResponse.StatusBreakdown.builder()
                        .status(1)
                        .statusLabel("Đang hoạt động")
                        .count(active)
                        .percentage(total > 0 ? Math.round(active * 10000.0 / total) / 100.0 : 0.0)
                        .build(),
                WarehouseAdminStatisticsResponse.StatusBreakdown.builder()
                        .status(0)
                        .statusLabel("Chờ duyệt")
                        .count(pending)
                        .percentage(total > 0 ? Math.round(pending * 10000.0 / total) / 100.0 : 0.0)
                        .build(),
                WarehouseAdminStatisticsResponse.StatusBreakdown.builder()
                        .status(2)
                        .statusLabel("Bị khóa")
                        .count(banned)
                        .percentage(total > 0 ? Math.round(banned * 10000.0 / total) / 100.0 : 0.0)
                        .build()
        );
    }

    private WarehouseSellerListResponse buildSellerWarehouseResponse(
            Page<Warehouse> warehousePage, Long sellerId) {

        List<WarehouseSellerResponse> warehouses = warehousePage.getContent().stream()
                .map(w -> warehouseMapper.toSellerResponse(w, inventoryQueryRepository))
                .toList();

        WarehouseSellerListResponse.Summary summary = WarehouseSellerListResponse.Summary.builder()
                .totalWarehouses(warehouseQueryRepository.countByManager(sellerId))
                .active(warehouseQueryRepository.countByManagerAndStatus(sellerId, ProductStatus.ACTIVE))
                .pending(warehouseQueryRepository.countByManagerAndStatus(sellerId, ProductStatus.PENDING))
                .banned(warehouseQueryRepository.countByManagerAndStatus(sellerId, ProductStatus.BAN))
                .build();

        return WarehouseSellerListResponse.builder()
                .warehouses(warehouses)
                .pagination(toSellerPagination(warehousePage))
                .summary(summary)
                .build();
    }

    private WarehouseAdminListResponse buildAdminWarehouseResponse(Page<Warehouse> warehousePage) {

        List<WarehouseAdminItemResponse> requests = warehousePage.getContent().stream()
                .map(warehouseMapper::toAdminResponse)
                .toList();

        return WarehouseAdminListResponse.builder()
                .requests(requests)
                .pagination(toAdminPagination(warehousePage))
                .build();
    }

    private WarehouseSellerListResponse.Pagination toSellerPagination(Page<?> page) {
        return WarehouseSellerListResponse.Pagination.builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .perPage(page.getSize())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .build();
    }

    private WarehouseAdminListResponse.Pagination toAdminPagination(Page<?> page) {
        return WarehouseAdminListResponse.Pagination.builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .perPage(page.getSize())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .build();
    }

    private Account getCurrentAccount() {
        return accountQueryRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateOwnership(Warehouse warehouse) {
        if (warehouse.getCreatedBy() == null ||
                !securityUtils.isOwner(warehouse.getCreatedBy().getEmail())) {
            throw new ForbiddenException("Bạn không có quyền truy cập kho này");
        }
    }
}