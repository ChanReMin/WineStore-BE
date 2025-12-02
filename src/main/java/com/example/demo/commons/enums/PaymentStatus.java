package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID(0, "Unpaid"),
    PAID(1, "Paid"),
//    REFUNDED(2, "Refunded"),
    FAILED(3, "Failed");

    private final int value; // Re-added this line
    private final String description;

    PaymentStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static PaymentStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.getValue() == value) { // Corrected to use getValue()
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PaymentStatus value: " + value);
    }
}