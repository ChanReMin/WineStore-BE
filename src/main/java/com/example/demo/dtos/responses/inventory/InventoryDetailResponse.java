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
public class InventoryDetailResponse {
    private Long id;
    private WarehouseDetailInfo warehouse;
    private ProductDetailInfo product;

    private Integer quantityOnHand;

    private Integer safetyStock;

    private Integer quantityReserved;

    private Integer quantityAvailable;

    private Integer quantityIncoming;

    private String status;

    private String locationInWarehouse;

    private LastTransactionInfo lastStockIn;

    private LastTransactionInfo lastStockOut;

    private LocalDateTime lastUpdatedAt;

    private String lastUpdatedBy;

    @Getter
    @Setter
    @Builder
    public static class WarehouseDetailInfo {
        private Long id;
        private String name;
        private String location;
        private String address;
        private String manager;
        private String phone;
    }

    @Getter
    @Setter
    @Builder
    public static class ProductDetailInfo {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
        private BigDecimal costPrice;
        private String image;
    }

    @Getter
    @Setter
    @Builder
    public static class LastTransactionInfo {
        private LocalDateTime date;
        private Integer quantity;
        private String note;
    }
}

