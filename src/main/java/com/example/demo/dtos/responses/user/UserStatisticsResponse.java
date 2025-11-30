package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsResponse {

    @JsonProperty("totalUsers")
    private Long totalUsers;

    @JsonProperty("activeUsers")
    private Long activeUsers;

    @JsonProperty("inactiveUsers")
    private Long inactiveUsers;

    @JsonProperty("bannedUsers")
    private Long bannedUsers;

    @JsonProperty("newUsersThisMonth")
    private Long newUsersThisMonth;

    @JsonProperty("usersByRole")
    private UsersByRole usersByRole;

    @JsonProperty("userGrowth")
    private List<UserGrowth> userGrowth;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsersByRole {
        private Long customer;
        private Long seller;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserGrowth {
        private LocalDate date;
        private Long count;
    }
}
