package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID(0),
    PAID(1),
    REFUNDED(2);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

}