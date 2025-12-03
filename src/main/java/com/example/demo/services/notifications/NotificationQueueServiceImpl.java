package com.example.demo.services.notifications;

import com.example.demo.dtos.notifications.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private final NotificationProducer notificationProducer;

    @Override
    @Async
    public void enqueue(NotificationMessage message) {
        notificationProducer.send(message);
    }
}