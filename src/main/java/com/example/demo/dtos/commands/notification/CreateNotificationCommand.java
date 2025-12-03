package com.example.demo.dtos.commands.notification;

import com.example.demo.commons.enums.NotificationStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateNotificationCommand {
    String title;
    String message;
    NotificationStatus status;
    String itemUrl;
}