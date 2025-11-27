package com.example.demo.dtos.commands.inventory;

import com.example.demo.commons.enums.InventoryLogType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInventoryLogRequest {

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Type is required")
    private String type; // Accept both String ("in", "out") and Integer (0, 1)

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Long shipmentId; // Optional, for tracking shipments/orders

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    private String referenceCode; // PO-2024-001, ORD-20241124-0001, etc.

    /**
     * Parse type string to InventoryLogType enum
     * Supports: "in", "out", "adjust", "return" or numeric codes 0-5
     */
    public InventoryLogType getTypeEnum() {
        if (type == null) {
            throw new IllegalArgumentException("Type is required");
        }

        // Try to parse as number first
        try {
            int code = Integer.parseInt(type);
            return InventoryLogType.fromCode(code);
        } catch (NumberFormatException e) {
            // Parse as string: "in", "out", "adjust", "return"
            return InventoryLogType.fromString(type.toUpperCase());
        }
    }
}