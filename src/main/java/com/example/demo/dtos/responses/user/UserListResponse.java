package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {
    private List<UserSummary> users;
    private Pagination pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private String id;
        private String email;
        private String name;
        private String phone;
        private String role;
        private String status;
        private String avatar;

        @JsonProperty("emailVerified")
        private Boolean emailVerified;

        @JsonProperty("createdAt")
        private LocalDateTime createdAt;

        @JsonProperty("lastLogin")
        private LocalDateTime lastLogin;

        @JsonProperty("totalOrders")
        private Integer totalOrders;

        @JsonProperty("totalSpent")
        private Long totalSpent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Pagination {
        @JsonProperty("currentPage")
        private Integer currentPage;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("totalUsers")
        private Long totalUsers;

        private Integer limit;
    }
}
