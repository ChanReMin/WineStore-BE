package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum AccountRole {
    CUSTOMER(0),
    SELLER(1),
    ADMIN(2);

    private final int value;

    AccountRole(int value) {
        this.value = value;
    }
}
