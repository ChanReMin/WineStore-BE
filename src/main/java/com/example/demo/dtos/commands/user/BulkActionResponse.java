package com.example.demo.dtos.commands.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionResponse {

    @JsonProperty("successCount")
    private Integer successCount;

    @JsonProperty("failedCount")
    private Integer failedCount;

    private List<BulkResult> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkResult {
        @JsonProperty("userId")
        private String userId;

        private Boolean success;

        @JsonProperty("errorMessage")
        private String errorMessage;
    }
}
