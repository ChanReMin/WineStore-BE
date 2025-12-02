package com.example.demo.dtos.mappers.warehouse;

import com.example.demo.dtos.responses.warehouse.WarehouseAdminItemResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseDetailResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseSellerResponse;
import com.example.demo.entities.InventoryLog;
import com.example.demo.entities.Warehouse;
import com.example.demo.repositories.queries.InventoryLogQueryRepository;
import com.example.demo.repositories.queries.InventoryQueryRepository;
import com.example.demo.repositories.queries.OrderQueryRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WarehouseMapper {

    /**
     * Map Warehouse to WarehouseDetailResponse for SELLER
     */
    public WarehouseDetailResponse toSellerDetailResponse(
            Warehouse warehouse,
            InventoryQueryRepository inventoryQueryRepository,
            InventoryLogQueryRepository inventoryLogQueryRepository) {

        if (warehouse == null) {
            return null;
        }

        // Get inventory summary (Seller need outOfStockProducts)
        Integer totalProducts = inventoryQueryRepository.countProductsInWarehouse(warehouse.getId());
        Integer totalQuantity = inventoryQueryRepository.countTotalInventory(warehouse.getId());
        BigDecimal totalValue = inventoryQueryRepository.calculateInventoryValue(warehouse.getId());
        Integer lowStockProducts = inventoryQueryRepository.countLowStockProducts(warehouse.getId());

        WarehouseDetailResponse.InventorySummary inventory =
                WarehouseDetailResponse.InventorySummary.builder()
                        .totalProducts(totalProducts)
                        .totalQuantity(totalQuantity)
                        .totalValue(totalValue != null ? totalValue.longValue() : 0L)
                        .lowStockProducts(lowStockProducts)
                        // outOfStockProducts = null cho seller
                        .build();

        // Get recent logs (Seller không có userName, referenceId, referenceType)
        List<InventoryLog> recentLogs = inventoryLogQueryRepository
                .findTop10ByWarehouseIdOrderByCreatedAtDesc(warehouse.getId());

        List<WarehouseDetailResponse.RecentLog> logs = recentLogs.stream()
                .map(log -> WarehouseDetailResponse.RecentLog.builder()
                        .id(log.getId())
                        .type(log.getType().name())
                        .productName(log.getProduct() != null ? log.getProduct().getName() : null)
                        .quantity(log.getQuantity())
                        // userName, referenceId, referenceType = null cho seller
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return WarehouseDetailResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus().getCode())
                .managerId(warehouse.getCreatedBy() != null ?
                        warehouse.getCreatedBy().getId() : null) // Seller chỉ có managerId
                .manager(null) // manager = null cho seller
                .inventory(inventory)
                .statistics(null) // Seller không có statistics
                .recentLogs(logs)
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    /**
     * Map Warehouse to WarehouseDetailResponse for ADMIN
     */
    public WarehouseDetailResponse toAdminDetailResponse(
            Warehouse warehouse,
            InventoryQueryRepository inventoryQueryRepository,
            InventoryLogQueryRepository inventoryLogQueryRepository,
            OrderQueryRepository orderQueryRepository) {

        if (warehouse == null) {
            return null;
        }

        // Get inventory summary (Admin có đầy đủ thông tin)
        Integer totalProducts = inventoryQueryRepository.countProductsInWarehouse(warehouse.getId());
        Integer totalQuantity = inventoryQueryRepository.countTotalInventory(warehouse.getId());
        BigDecimal totalValue = inventoryQueryRepository.calculateInventoryValue(warehouse.getId());
        Integer lowStockProducts = inventoryQueryRepository.countLowStockProducts(warehouse.getId());
        Integer outOfStockProducts = inventoryQueryRepository.countOutOfStockProducts(warehouse.getId());

        WarehouseDetailResponse.InventorySummary inventory =
                WarehouseDetailResponse.InventorySummary.builder()
                        .totalProducts(totalProducts)
                        .totalQuantity(totalQuantity)
                        .totalValue(totalValue != null ? totalValue.longValue() : 0L)
                        .lowStockProducts(lowStockProducts)
                        .outOfStockProducts(outOfStockProducts) // Admin có field này
                        .build();

        // Get recent logs (Admin có đầy đủ thông tin)
        List<InventoryLog> recentLogs = inventoryLogQueryRepository
                .findTop10ByWarehouseIdOrderByCreatedAtDesc(warehouse.getId());

        List<WarehouseDetailResponse.RecentLog> logs = recentLogs.stream()
                .map(log -> {
                    String userName = null;
                    if (log.getUser() != null) {
                        userName = log.getUser().getFirstName() + " " + log.getUser().getLastName();
                    }

                    return WarehouseDetailResponse.RecentLog.builder()
                            .id(log.getId())
                            .type(log.getType().name())
                            .productName(log.getProduct() != null ? log.getProduct().getName() : null)
                            .quantity(log.getQuantity())
                            .userName(userName) // Admin có userName
                            .createdAt(log.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        // Get statistics (Admin có statistics)
        WarehouseDetailResponse.Statistics statistics =
                calculateWarehouseStatistics(warehouse.getId(), orderQueryRepository);

        // Manager info (Admin có đầy đủ thông tin manager)
        WarehouseDetailResponse.ManagerInfo managerInfo = null;
        if (warehouse.getCreatedBy() != null) {
            var manager = warehouse.getCreatedBy();
            var user = manager.getUser();

            managerInfo = WarehouseDetailResponse.ManagerInfo.builder()
                    .id(user != null ? user.getId() : null)
                    .accountId(manager.getId())
                    .email(manager.getEmail())
                    .firstName(user != null ? user.getFirstName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .phoneNumber(user != null ? user.getPhoneNumber() : null)
                    .role(manager.getRole().ordinal())
                    .createdAt(manager.getCreatedAt())
                    .build();
        }

        return WarehouseDetailResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus().getCode())
                .managerId(null)
                .manager(managerInfo)
                .inventory(inventory)
                .statistics(statistics)
                .recentLogs(logs)
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    /**
     * Map Warehouse to WarehouseSellerResponse with inventory summary
     */
    public WarehouseSellerResponse toSellerResponse(
            Warehouse warehouse,
            InventoryQueryRepository inventoryQueryRepository) {

        if (warehouse == null) {
            return null;
        }

        Integer totalProducts = inventoryQueryRepository.countProductsInWarehouse(warehouse.getId());
        Integer totalQuantity = inventoryQueryRepository.countTotalInventory(warehouse.getId());
        BigDecimal totalValue = inventoryQueryRepository.calculateInventoryValue(warehouse.getId());
        Integer lowStockProducts = inventoryQueryRepository.countLowStockProducts(warehouse.getId());
        Integer outOfStockProducts = inventoryQueryRepository.countOutOfStockProducts(warehouse.getId());

        WarehouseSellerResponse.InventorySummary inventorySummary =
                WarehouseSellerResponse.InventorySummary.builder()
                        .totalProducts(totalProducts)
                        .totalQuantity(totalQuantity)
                        .totalValue(totalValue != null ? totalValue.longValue() : 0L)
                        .lowStockProducts(lowStockProducts)
                        .outOfStockProducts(outOfStockProducts)
                        .build();

        return WarehouseSellerResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus().getCode())
                .inventorySummary(inventorySummary)
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    /**
     * Map Warehouse to WarehouseResponse (for general use)
     */
    public WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus().getCode())
                .manager(toManagerInfo(warehouse))
                .inventorySummary(null)
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    /**
     * Map Warehouse to WarehouseAdminItemResponse
     */
    public WarehouseAdminItemResponse toAdminResponse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        WarehouseAdminItemResponse.ManagerInfo managerInfo = null;

        if (warehouse.getCreatedBy() != null && warehouse.getCreatedBy().getUser() != null) {
            managerInfo = WarehouseAdminItemResponse.ManagerInfo.builder()
                    .id(warehouse.getCreatedBy().getId())
                    .email(warehouse.getCreatedBy().getEmail())
                    .firstName(warehouse.getCreatedBy().getUser().getFirstName())
                    .lastName(warehouse.getCreatedBy().getUser().getLastName())
                    .phoneNumber(warehouse.getCreatedBy().getUser().getPhoneNumber())
                    .build();
        }

        return WarehouseAdminItemResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus().getCode())
                .manager(managerInfo)
                .createdAt(warehouse.getCreatedAt() != null ? warehouse.getCreatedAt().toString() : null)
                .updatedAt(warehouse.getUpdatedAt() != null ? warehouse.getUpdatedAt().toString() : null)
                .build();
    }

    /**
     * Map to ManagerInfo
     */
    private WarehouseResponse.ManagerInfo toManagerInfo(Warehouse warehouse) {
        if (warehouse.getCreatedBy() == null) {
            return null;
        }

        var manager = warehouse.getCreatedBy();
        var user = manager.getUser();

        return WarehouseResponse.ManagerInfo.builder()
                .id(user != null ? user.getId() : null)
                .accountId(manager.getId())
                .email(manager.getEmail())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .role(manager.getRole().ordinal())
                .createdAt(manager.getCreatedAt())
                .build();
    }

    /**
     * Calculate warehouse statistics from orders
     */
    private WarehouseDetailResponse.Statistics calculateWarehouseStatistics(
            Long warehouseId,
            OrderQueryRepository orderQueryRepository) {

        // Tính tổng số đơn hàng từ warehouse này
        Long totalOrders = orderQueryRepository.countOrdersFromWarehouse(warehouseId);

        // Tính tổng doanh thu từ warehouse này
        BigDecimal totalRevenue = orderQueryRepository.calculateRevenueFromWarehouse(warehouseId);

        // Tính số đơn hàng trong 30 ngày qua
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Long last30DaysOrders = orderQueryRepository.countOrdersFromWarehouseSince(
                warehouseId,
                thirtyDaysAgo
        );

        return WarehouseDetailResponse.Statistics.builder()
                .totalOrdersFromThisWarehouse(totalOrders != null ? totalOrders.intValue() : 0)
                .totalRevenue(totalRevenue != null ? totalRevenue.longValue() : 0L)
                .last30DaysOrders(last30DaysOrders != null ? last30DaysOrders.intValue() : 0)
                .build();
    }
}