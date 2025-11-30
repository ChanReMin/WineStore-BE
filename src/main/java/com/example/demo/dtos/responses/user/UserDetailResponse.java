package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {
    private String id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String status;
    private String avatar;

    @JsonProperty("emailVerified")
    private Boolean emailVerified;

    private Address address;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("lastLogin")
    private LocalDateTime lastLogin;

    private Statistics statistics;

    private String notes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String street;
        private String ward;
        private String district;
        private String city;
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistics {
        @JsonProperty("totalOrders")
        private Integer totalOrders;

        @JsonProperty("totalSpent")
        private Long totalSpent;

        @JsonProperty("totalProducts")
        private Integer totalProducts;
    }
}
