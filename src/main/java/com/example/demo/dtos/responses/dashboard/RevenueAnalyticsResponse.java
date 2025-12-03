package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {
    private List<RevenueChartDTO> revenueChart;

    private Map<String, PaymentMethodStatsDTO> paymentMethods;

    private List<CategoryPerformanceDTO> categoriesPerformance;

    private List<RegionPerformanceDTO> regionsPerformance;
}