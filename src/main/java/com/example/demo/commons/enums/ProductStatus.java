package com.example.demo.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProductStatus {
    PENDING(0, "Pending"),
    ACTIVE(1, "Active"),
    BAN(2, "Ban");

    private final Integer code;
    private final String description;

    ProductStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @JsonValue tells Jackson to serialize this enum using the 'code' field
     * Example: ProductStatus.PENDING will be serialized as 0 in JSON
     */
    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * @JsonCreator tells Jackson how to deserialize JSON back to this enum
     * Example: When JSON contains "status": 0, it will be converted to ProductStatus.PENDING
     */
    @JsonCreator
    public static ProductStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid product status code: " + code);
    }
}