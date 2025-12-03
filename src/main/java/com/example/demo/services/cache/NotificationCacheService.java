package com.example.demo.services.cache;

import com.example.demo.dtos.responses.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.example.demo.services.queries.NotificationQueryService;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCacheService {

    private final NotificationQueryService notificationQueryService;

    /**
     * Cache notifications trong 5 phút (300 giây)
     * Key format: notifications:userId:page:size
     */
    @Cacheable(value = "notifications", key = "'user_' + #userId + '_page_' + #page + '_size_' + #size",
            unless = "#result == null || #result.isEmpty()")
    public List<NotificationResponse> getUserNotificationsWithCache(Long userId, int page, int size) {
        return notificationQueryService.getUserNotifications(userId, page, size).getContent();
    }

    @Cacheable(value = "allNotifications", key = "'user_' + #userId",
            unless = "#result == null || #result.isEmpty()")
    public List<NotificationResponse> getAllNotificationsWithCache(Long userId) {
        return notificationQueryService.getUserNotifications(userId, 0, Integer.MAX_VALUE).getContent();
    }

    /**
     * Cache unread count trong 1 phút (60 giây)
     * Key format: unread_count:userId
     */
    @Cacheable(value = "unreadCount", key = "'user_' + #userId",
            unless = "#result == null")
    public long getUnreadCountWithCache(Long userId) {
        return notificationQueryService.countUnread(userId);
    }

    /**
     * Clear cache khi có notification mới
     */
    @CacheEvict(value = {"notifications", "unreadCount"}, allEntries = true)
    public void invalidateCache() {
    }

    /**
     * Clear cache cho specific user
     */
    @CacheEvict(value = {"notifications", "unreadCount"}, key = "'user_' + #userId")
    public void invalidateCacheForUser(Long userId) {
    }
}