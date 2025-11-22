package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING(1),
    SUCCESS(2),
    FAILED(3);

    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

}