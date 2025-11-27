package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Data
@Builder
public class WarehouseSellerResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status; // 0, 1, 2


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private InventorySummary inventorySummary;


    @Getter
    @Setter
    @Builder
    public static class InventorySummary {
        private Integer totalProducts;
        private Integer totalQuantity;
        private Long totalValue;
        private Integer lowStockProducts;
        private Integer outOfStockProducts;
    }
}
