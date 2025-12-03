package com.example.demo.dtos.responses.notification;

import com.example.demo.commons.enums.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class NotificationResponse {

    Long id;

    Long userId;

    String title;

    String message;

    NotificationStatus status;

    @JsonProperty("isRead")
    boolean isRead;

    String itemUrl;

    Instant createdAt;
}