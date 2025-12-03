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



import java.math.BigDecimal;

import java.time.LocalDate;

import java.time.LocalDateTime;

import java.time.ZoneOffset;

import java.time.temporal.TemporalAdjusters;

import java.util.List;

import java.util.stream.Collectors;



@Service

@RequiredArgsConstructor

public class SellerDashboardQueryServiceImpl implements SellerDashboardQueryService {



    private final OrderQueryRepository orderQueryRepository;

    private final InventoryQueryRepository inventoryQueryRepository;



    @Override

    public SellerDashboardOverviewResponse getSellerDashboardOverview() {

        long totalOrders = orderQueryRepository.count();

        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue();

        long pendingOrders = orderQueryRepository.countByStatus(OrderStatus.PENDING);

        long completedOrders = orderQueryRepository.countByStatus(OrderStatus.CONFIRMED); // Assuming CONFIRMED is completed

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

            switch (period) {

                case "weekly":

                    startDate = LocalDate.now().minusWeeks(4);

                    break;

                case "monthly":

                    startDate = LocalDate.now().minusMonths(6);

                    break;

                case "daily":

                default:

                    startDate = LocalDate.now().minusDays(7);

                    break;

            }

        }

        if (endDate == null) {

            endDate = LocalDate.now();

        }



        // Adjust dates for queries

        LocalDateTime startDateTime = startDate.atStartOfDay();

        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);



        List<Object[]> rawChartData = orderQueryRepository.findRevenueAndOrdersByPeriod(startDateTime, endDateTime, period);



        List<SellerRevenueChartDataResponse> chartData = rawChartData.stream()

                .map(obj -> SellerRevenueChartDataResponse.builder()

                        .date((String) obj[0])

                        .revenue(((BigDecimal) obj[1]).doubleValue())

                        .orders(((Long) obj[2]))

                        .build())

                .collect(Collectors.toList());



        double totalRevenue = chartData.stream().mapToDouble(SellerRevenueChartDataResponse::getRevenue).sum();

        long totalOrders = chartData.stream().mapToLong(SellerRevenueChartDataResponse::getOrders).sum();



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

        

        // Determine status string

        String statusString;

        if (inventory.getQuantityOnHand() == null || inventory.getQuantityOnHand() <= 0) {

            statusString = "outOfStock";

        } else if (inventory.getQuantityOnHand() <= inventory.getSafetyStock()) {

            statusString = "lowStock";

        } else {

            statusString = "inStock"; // Should not happen with findLowAndOutOfStockInventory

        }



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

}
