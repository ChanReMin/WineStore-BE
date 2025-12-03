package com.example.demo.dtos.requests.notification;

import com.example.demo.commons.enums.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private NotificationStatus status;

    private String itemUrl;
}