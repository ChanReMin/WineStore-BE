package com.example.demo.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum InventoryStatus {
    IN_STOCK("in_stock", "Còn hàng"),
    LOW_STOCK("low_stock", "Tồn kho thấp"),
    OUT_OF_STOCK("out_of_stock", "Hết hàng");

    private final String code;
    private final String description;

    InventoryStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Serialize to JSON as string code (e.g., "in_stock")
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * Deserialize from JSON string
     */
    @JsonCreator
    public static InventoryStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (InventoryStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid inventory status code: " + code);
    }

    /**
     * Calculate inventory status based on quantity and safety stock
     *
     * @param quantityOnHand Current quantity in stock
     * @param safetyStock Minimum safe stock level
     * @return InventoryStatus
     */
    public static InventoryStatus calculateStatus(Integer quantityOnHand, Integer safetyStock) {
        if (quantityOnHand == null || quantityOnHand <= 0) {
            return OUT_OF_STOCK;
        }

        if (safetyStock != null && quantityOnHand <= safetyStock) {
            return LOW_STOCK;
        }

        return IN_STOCK;
    }
}