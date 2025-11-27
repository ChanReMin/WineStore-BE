package com.example.demo.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum InventoryLogType {
    IN(0, "Nhập kho"),
    OUT(1, "Xuất kho"),
    ADJUST(2, "Điều chỉnh"),
    RETURN(3, "Hoàn trả"),
    TRANSFER_OUT(4, "Xuất chuyển kho"),
    TRANSFER_IN(5, "Nhập từ kho khác");

    private final int code;
    private final String description;

    InventoryLogType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    public static InventoryLogType fromCode(int code) {
        for (InventoryLogType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid InventoryLogType code: " + code);
    }

    public static InventoryLogType fromString(String str) {
        for (InventoryLogType type : values()) {
            if (type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid InventoryLogType: " + str);
    }
}