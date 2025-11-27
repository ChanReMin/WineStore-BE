package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferInventoryResponse {
    private Long transferId;

    private String transferCode;

    private Long productId;

    private Long fromWarehouseId;

    private Long toWarehouseId;

    private Integer quantity;
    private String status;
    private String statusText;

    private LocalDateTime createdAt;
}