package com.example.demo.controllers;

import com.example.demo.commons.enums.NotificationStatus;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.notifications.NotificationMessage;
import com.example.demo.dtos.responses.notification.NotificationResponse;
import com.example.demo.services.commands.NotificationCommandService;
import com.example.demo.services.notifications.NotificationProducer;
import com.example.demo.services.queries.NotificationQueryService;
import com.example.demo.services.cache.NotificationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCacheService notificationCacheService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationProducer notificationProducer;

    /**
     * ✅ API mới: Lấy tất cả notifications (không paging)
     * Dùng cho header dropdown
     */
    @GetMapping("/all")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        List<NotificationResponse> notifications = notificationCacheService.getAllNotificationsWithCache(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * ⏳ API cũ: Lấy notifications với paging
     * Dùng cho trang dedicated /notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.getCurrentUserUuid();
        Page<NotificationResponse> result = notificationQueryService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        long count = notificationCacheService.getUnreadCountWithCache(userId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserUuid();
        notificationCommandService.markAsRead(userId, id);
        notificationCacheService.invalidateCacheForUser(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        int updated = notificationCommandService.markAllAsRead(userId);
        notificationCacheService.invalidateCacheForUser(userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllNotifications() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        notificationCommandService.clearAllNotifications(userId);
        notificationCacheService.invalidateCacheForUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-sqs")
    public ResponseEntity<String> testSqsFlow() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        NotificationMessage msg = NotificationMessage.builder()
                .id(0L)
                .userId(userId)
                .title("Test Notification")
                .message("This is a test notification")
                .status(NotificationStatus.SUCCESS)
                .itemUrl("https://example.com/test/item/123")
                .createdAt(Instant.now())
                .build();

        notificationProducer.send(msg);
        notificationCacheService.invalidateCacheForUser(userId);
        return ResponseEntity.ok("Test notification sent");
    }
}