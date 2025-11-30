package com.example.demo.commons.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE(0, "Active"),
    INACTIVE(1, "Inactive"),
    LOCKED(2, "Locked");

    private final int code;
    private final String displayName;

    AccountStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static AccountStatus fromCode(int code) {
        for (AccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }

    public static AccountStatus fromString(String status) {
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}