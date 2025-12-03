package com.example.demo.dtos.responses.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class SellerInventoryAlertResponse {
    private Long id;
    private SellerInventoryAlertWarehouseResponse warehouse;
    private SellerInventoryAlertProductResponse product;
    private Integer quantityOnHand;
    private Integer safetyStock;
    private String status; // "lowStock", "outOfStock"
    private OffsetDateTime lastUpdatedAt;
}
