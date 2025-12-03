package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(1, "Pending Confirmation"),
    CONFIRMED(2, "Confirmed"),
    CANCELLED(6, "Cancelled"),
    PAID(3, "Paid");

    private final int value;
    private final String description;

    OrderStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static OrderStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.getValue() == value) { // Corrected to use getValue()
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
    }

}