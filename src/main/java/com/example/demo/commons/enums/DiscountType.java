package com.example.demo.commons.enums;

public enum DiscountType {
    PERCENTAGE(1),
    FIXED_AMOUNT(2);

    private final int value;

    DiscountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
