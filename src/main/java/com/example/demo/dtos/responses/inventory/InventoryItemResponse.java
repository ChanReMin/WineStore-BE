package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryItemResponse {
    private Long id;
    private WarehouseInfo warehouse;
    private ProductInfo product;

    private Integer quantityOnHand;

    private Integer safetyStock;

    private Integer quantityReserved;

    private Integer quantityAvailable;

    private String status; // in_stock, low_stock, out_of_stock

    private LocalDateTime lastUpdatedAt;

    private String lastUpdatedBy;

    @Getter
    @Setter
    @Builder
    public static class WarehouseInfo {
        private Long id;
        private String name;
        private String location;
        private String address;
    }

    @Getter
    @Setter
    @Builder
    public static class ProductInfo {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private String image;
    }
}