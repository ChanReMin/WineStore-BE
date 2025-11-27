package com.example.demo.dtos.commands.inventory;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInventoryRequest {

    @NotNull(message = "Type is required")
    @Pattern(regexp = "^(in|out|adjust)$", message = "Type must be: in, out, or adjust")
    private String type; // in, out, adjust

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 100, message = "Reference code must not exceed 100 characters")
    private String referenceCode; // PO-2024-001, SO-2024-001, etc.
}
