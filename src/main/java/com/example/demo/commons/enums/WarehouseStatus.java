package com.example.demo.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WarehouseStatus {
    PENDING(0, "Pending"),
    APPROVE(1, "Approve"),
    REJECT(2, "Reject"),
    BAN(3, "Ban");

    private final Integer code;
    private final String description;

    WarehouseStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @JsonValue tells Jackson to serialize this enum using the 'code' field
     * Example: WarehouseStatus.PENDING will be serialized as 0 in JSON
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * @JsonCreator tells Jackson how to deserialize JSON back to this enum
     * Example: When JSON contains "status": 0, it will be converted to WarehouseStatus.PENDING
     */
    @JsonCreator
    public static WarehouseStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WarehouseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid product status code: " + code);
    }
}
