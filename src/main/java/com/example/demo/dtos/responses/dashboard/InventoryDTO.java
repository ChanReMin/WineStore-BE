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
public class InventoryDTO {
    private BigDecimal totalValue;

    private Integer totalQuantity;

    private Long totalWarehouses;

    private Long lowStockProducts;

    private Long outOfStockProducts;
}
