package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertsResponse {
    private List<AlertInfo> alerts;
    private AlertSummary summary;

    @Getter
    @Setter
    @Builder
    public static class AlertInfo {
        private Long id;
        private String type; // out_of_stock, low_stock
        private String typeText;
        private String severity; // critical, warning
        private WarehouseInfo warehouse;
        private ProductInfo product;
        private Integer currentQuantity;
        private Integer safetyStock;
        private String message;
        private LocalDateTime createdAt;

        @Getter
        @Setter
        @Builder
        public static class WarehouseInfo {
            private Long id;
            private String name;
        }

        @Getter
        @Setter
        @Builder
        public static class ProductInfo {
            private Long id;
            private String name;
            private String sku;
            private String image;
        }
    }

    @Getter
    @Setter
    @Builder
    public static class AlertSummary {
        private Integer totalAlerts;
        private Integer critical;
        private Integer warning;
    }
}
