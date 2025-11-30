package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityResponse {
    private List<Activity> activities;
    private Pagination pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Activity {
        private String id;

        @JsonProperty("userId")
        private String userId;

        private String type;
        private String action;

        @JsonProperty("ipAddress")
        private String ipAddress;

        @JsonProperty("userAgent")
        private String userAgent;

        @JsonProperty("createdAt")
        private LocalDateTime createdAt;

        private MetadataActivity metadata;
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

        @JsonProperty("totalActivities")
        private Long totalActivities;

        private Integer limit;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetadataActivity {
        @JsonProperty("orderId")
        private String orderId;

        private Long amount;
    }
}
