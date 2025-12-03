package com.example.demo.services.queries;

import com.example.demo.dtos.mappers.notification.NotificationMapper;
import com.example.demo.dtos.responses.notification.NotificationResponse;
import com.example.demo.repositories.queries.NotificationQueryRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationQueryRepository notificationQueryRepository;
    private final NotificationMapper notificationMapper;

    public NotificationQueryService(NotificationQueryRepository notificationQueryRepository,
                                    NotificationMapper notificationMapper)
    {
        this.notificationQueryRepository = notificationQueryRepository;
        this.notificationMapper = notificationMapper;
    }

    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationQueryRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    public long countUnread(Long userId) {
        return notificationQueryRepository.countByUser_IdAndIsReadFalse(userId);
    }
}