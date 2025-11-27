package com.example.demo.dtos.commands.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferInventoryRequest {
    private Long productId;

    private Long fromWarehouseId;

    private Long toWarehouseId;

    private Integer quantity;
    private String note;
}
