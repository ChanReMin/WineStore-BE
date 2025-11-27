package com.example.demo.dtos.responses.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ============= Inventory Log Response =============
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogResponse {
    private Long id;
    private String type; // "in", "out", "adjust", "return", "transfer_out", "transfer_in"

    private String typeText; // "Nhập kho", "Xuất kho", etc.

    private WarehouseInfo warehouse;
    private ProductInfo product;

    private Integer quantityBefore;

    private Integer quantityChange;

    private Integer quantityAfter;

    private String note;

    private String referenceCode;

    private String createdBy;

    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseInfo {
        private Long id;
        private String name;
        private String location;
        private String address;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInfo {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private String image;
    }
}