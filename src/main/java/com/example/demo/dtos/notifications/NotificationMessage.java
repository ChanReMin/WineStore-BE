package com.example.demo.dtos.notifications;

import com.example.demo.commons.enums.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationMessage {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private NotificationStatus status;

    @JsonProperty("itemUrl")
    private String itemUrl;

    @JsonProperty("createdAt")
    private Instant createdAt;
}