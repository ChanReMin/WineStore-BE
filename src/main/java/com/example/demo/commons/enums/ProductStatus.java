package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    PENDING(1, "Pending"),
    ACTIVE(2, "Active"),
    BAN(3, "Ban");

    private final Integer code;
    private final String description;

    ProductStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

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