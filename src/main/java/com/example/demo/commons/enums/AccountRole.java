package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum AccountRole {
    CUSTOMER(0, "Customer"),
    SELLER(1, "Seller"),
    ADMIN(2, "Admin");

    private final int code;
    private final String displayName;

    AccountRole(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static AccountRole fromCode(int code) {
        for (AccountRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }

    public static AccountRole fromString(String role) {
        try {
            return AccountRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}