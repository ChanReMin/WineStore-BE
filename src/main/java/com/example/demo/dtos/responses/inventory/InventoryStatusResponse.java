package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStatusResponse {
    private Long productId;

    private String productName;

    private Long warehouseId;

    private String warehouseName;

    private Integer quantityOnHand;

    private Integer safetyStock;

    private Boolean isLowStock;

    private LocalDateTime lastUpdatedAt;
}
