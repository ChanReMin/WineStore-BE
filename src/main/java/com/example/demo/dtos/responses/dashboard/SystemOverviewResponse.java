package com.example.demo.dtos.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewResponse {
    private PeriodDTO period;
    private BusinessMetricsDTO businessMetrics;

    private UsersDTO users;
    private ProductsDTO products;
    private OrdersDTO orders;
    private InventoryDTO inventory;
}