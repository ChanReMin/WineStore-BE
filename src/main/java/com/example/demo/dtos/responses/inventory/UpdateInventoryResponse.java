package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryResponse {
    private Long inventoryId;

    private Long warehouseId;

    private Long productId;

    private Integer oldQuantity;

    private Integer newQuantity;

    private Integer quantityChange;

    private String type;

    private Long logId;

    private LocalDateTime updatedAt;
}