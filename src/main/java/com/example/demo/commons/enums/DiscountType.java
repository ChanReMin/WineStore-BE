package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum DiscountType {
    PERCENTAGE(1),
    FIXED_AMOUNT(2);

    private final int value;

    DiscountType(int value) {
        this.value = value;
    }

}
