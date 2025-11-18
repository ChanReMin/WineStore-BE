package com.example.demo.commons.enums;

public enum TransactionStatus {
    PENDING(1),
    SUCCESS(2),
    FAILED(3);

    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}