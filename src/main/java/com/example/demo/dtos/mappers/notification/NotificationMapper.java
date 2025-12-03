package com.example.demo.dtos.mappers.notification;

import com.example.demo.dtos.commands.notification.CreateNotificationCommand;
import com.example.demo.dtos.requests.notification.NotificationCreateRequest;
import com.example.demo.dtos.responses.notification.NotificationResponse;
import com.example.demo.entities.Notification;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    /**
     * Request -> Command (if still using)
     */
    public CreateNotificationCommand toCommand(NotificationCreateRequest request) {
        if (request == null) return null;
        return CreateNotificationCommand.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .status(request.getStatus())
                .itemUrl(request.getItemUrl())
                .build();
    }

    /**
     * Entity -> Response
     */
    public NotificationResponse toResponse(Notification n) {
        if (n == null) return null;

        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .title(n.getTitle())
                .message(n.getMessage())
                .status(n.getStatus())
                .isRead(n.isRead())
                .itemUrl(n.getItemUrl())
                .createdAt(n.getCreatedAt() != null
                        ? n.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                        : null)
                .build();
    }

    /**
     * Entity List -> Response List
     */
    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) return List.of();
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}