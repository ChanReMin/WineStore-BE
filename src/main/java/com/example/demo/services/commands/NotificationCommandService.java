package com.example.demo.services.commands;

import com.example.demo.dtos.mappers.notification.NotificationMapper;
import com.example.demo.dtos.notifications.NotificationMessage;
import com.example.demo.dtos.responses.notification.NotificationResponse;
import com.example.demo.entities.Notification;
import com.example.demo.entities.User;
import com.example.demo.repositories.commands.NotificationCommandRepository;
import com.example.demo.repositories.queries.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationCommandRepository notificationCommandRepository;
    private final UserQueryRepository userQueryRepository;
    private final NotificationMapper notificationMapper;

    public NotificationResponse saveNotification(NotificationMessage message) {
        Long userId = message.getUserId();

        if (userId == null) {
            return null;
        }

        User user = userQueryRepository.findById(message.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + message.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(message.getTitle())
                .message(message.getMessage())
                .status(message.getStatus())
                .itemUrl(message.getItemUrl())
                .isRead(false)
                .build();

        notification = notificationCommandRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    public void clearAllNotifications(Long userId) {
        notificationCommandRepository.deleteByUserId(userId);
    }
    public void markAsRead(Long userId, Long notificationId) {
        notificationCommandRepository.markAsRead(userId, notificationId);
    }
    public int markAllAsRead(Long userId) {
        int updated = notificationCommandRepository.markAllAsRead(userId);
        return updated;
    }
}