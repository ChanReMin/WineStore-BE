package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.commons.enums.InventoryStatus;
import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.dtos.responses.dashboard.SellerDashboardOverviewResponse;
import com.example.demo.dtos.responses.dashboard.SellerInventoryAlertProductResponse;
import com.example.demo.dtos.responses.dashboard.SellerInventoryAlertResponse;
import com.example.demo.dtos.responses.dashboard.SellerInventoryAlertWarehouseResponse;
import com.example.demo.dtos.responses.dashboard.SellerRevenueChartDataResponse;
import com.example.demo.dtos.responses.dashboard.SellerRevenueResponse;
import com.example.demo.entities.Inventory;
import com.example.demo.entities.Product;
import com.example.demo.entities.Warehouse;
import com.example.demo.repositories.queries.InventoryQueryRepository;
import com.example.demo.repositories.queries.OrderQueryRepository;
import com.example.demo.services.queries.SellerDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerDashboardQueryServiceImpl implements SellerDashboardQueryService {

    private final OrderQueryRepository orderQueryRepository;
    private final InventoryQueryRepository inventoryQueryRepository;

    @Override
    public SellerDashboardOverviewResponse getSellerDashboardOverview() {
        long totalOrders = orderQueryRepository.count();
        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue();
        long pendingOrders = orderQueryRepository.countByStatus(OrderStatus.PENDING);
        long completedOrders = orderQueryRepository.countByStatus(OrderStatus.CONFIRMED);
        long cancelledOrders = orderQueryRepository.countByStatus(OrderStatus.CANCELLED);
        long lowStockProducts = inventoryQueryRepository.countLowStockProducts();
        long outOfStockProducts = inventoryQueryRepository.countOutOfStockProducts();

        return SellerDashboardOverviewResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }

    @Override
    public SellerRevenueResponse getSellerRevenue(String period, LocalDate startDate, LocalDate endDate) {
        // Set default dates if not provided
        if (startDate == null) {
            startDate = getDefaultStartDate(period);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Adjust dates for queries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Fetch revenue data
        List<Object[]> rawChartData = orderQueryRepository.findRevenueAndOrdersByPeriod(
                startDateTime, endDateTime, period
        );

        // Map to response DTOs
        List<SellerRevenueChartDataResponse> chartData = rawChartData.stream()
                .map(obj -> SellerRevenueChartDataResponse.builder()
                        .date((String) obj[0])
                        .revenue(((BigDecimal) obj[1]).doubleValue())
                        .orders(((Long) obj[2]))
                        .build())
                .collect(Collectors.toList());

        // Calculate totals
        double totalRevenue = chartData.stream()
                .mapToDouble(SellerRevenueChartDataResponse::getRevenue)
                .sum();
        long totalOrders = chartData.stream()
                .mapToLong(SellerRevenueChartDataResponse::getOrders)
                .sum();

        return SellerRevenueResponse.builder()
                .period(period)
                .chartData(chartData)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .build();
    }

    @Override
    public List<SellerInventoryAlertResponse> getInventoryAlerts() {
        List<Inventory> alerts = inventoryQueryRepository.findLowAndOutOfStockInventory();

        return alerts.stream()
                .map(this::mapToSellerInventoryAlertResponse)
                .collect(Collectors.toList());
    }

    private SellerInventoryAlertResponse mapToSellerInventoryAlertResponse(Inventory inventory) {
        Product product = inventory.getProduct();
        Warehouse warehouse = inventory.getWarehouse();

        SellerInventoryAlertProductResponse productResponse = SellerInventoryAlertProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();

        SellerInventoryAlertWarehouseResponse warehouseResponse = SellerInventoryAlertWarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .build();

        String statusString = determineInventoryStatus(inventory);

        return SellerInventoryAlertResponse.builder()
                .id(inventory.getId())
                .warehouse(warehouseResponse)
                .product(productResponse)
                .quantityOnHand(inventory.getQuantityOnHand())
                .safetyStock(inventory.getSafetyStock())
                .status(statusString)
                .lastUpdatedAt(inventory.getLastUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }

    private String determineInventoryStatus(Inventory inventory) {
        if (inventory.getQuantityOnHand() == null || inventory.getQuantityOnHand() <= 0) {
            return "outOfStock";
        } else if (inventory.getQuantityOnHand() <= inventory.getSafetyStock()) {
            return "lowStock";
        } else {
            return "inStock";
        }
    }

    private LocalDate getDefaultStartDate(String period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case "weekly" -> today.minusWeeks(4);
            case "monthly" -> today.minusMonths(6);
            default -> today.minusDays(7); // daily
        };
    }
}