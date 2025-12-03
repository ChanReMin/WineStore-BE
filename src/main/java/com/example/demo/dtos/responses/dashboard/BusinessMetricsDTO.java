package com.example.demo.dtos.responses.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMetricsDTO {
    private BigDecimal totalRevenue;

    private BigDecimal grossProfit;

    private BigDecimal profitMarginPercent;

    private Long totalOrders;

    private BigDecimal averageOrderValue;

    private BigDecimal conversionRate;
}
