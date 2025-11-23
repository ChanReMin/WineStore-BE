package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(1),
    CONFIRMED(2),
    PAID(3),
    SHIPPING(4),
    DELIVERED(5),
    CANCELLED(6),
    RETURNED(7);

    private final int value;

    OrderStatus(int value) {
        this.value = value;
    }

}