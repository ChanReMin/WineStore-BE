package com.example.demo.dtos.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SellerRevenueResponse {
    private String period;
    @JsonProperty("chart_data")
    private List<SellerRevenueChartDataResponse> chartData;
    private double totalRevenue;
    private long totalOrders;
}
