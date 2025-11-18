package com.example.demo.commons.enums;

public enum PaymentStatus {
    UNPAID(0),
    PAID(1),
    REFUNDED(2);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}