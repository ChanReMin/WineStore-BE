package com.example.demo.dtos.commands.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferInventoryRequest {
    @Positive(message = "Product ID must be a positive number")
    private Long productId;

    @Positive(message = "From Warehouse ID must be a positive number")
    private Long fromWarehouseId;

    @Positive(message = "To Warehouse ID must be a positive number")
    private Long toWarehouseId;

    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;

    @Size(max = 1000, message = "Note cannot exceed 1000 characters")
    private String note;
}
