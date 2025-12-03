package com.example.demo.dtos.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerDashboardOverviewResponse {
    private long totalOrders;
    private double totalRevenue;
    @JsonProperty("pending_orders")
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long lowStockProducts;
    @JsonProperty("outOfStock_products")
    private long outOfStockProducts;
}
