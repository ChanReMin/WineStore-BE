package com.example.demo.dtos.responses.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogDetailResponse {

    private Long id;
    private String type;
    private String typeText;
    private Integer quantity;
    private LocalDateTime createdAt;
    private WarehouseInfo warehouse;
    private ProductInfo product;
    private QuantityChange quantityChange;
    private UserInfo user;
    private String note;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseInfo {
        private Long id;
        private String name;
        private String location;
        private String city;
        private ManagerInfo manager;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ManagerInfo {
            private String name;
            private String phone;
        }
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
        private BigDecimal costPrice;
        private String image;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuantityChange {
        private Integer before;
        private Integer change;
        private Integer after;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String role;
    }
}
