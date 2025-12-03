package com.example.demo.services.notifications;
import com.example.demo.dtos.notifications.NotificationMessage;

public interface NotificationQueueService {
    void enqueue(NotificationMessage notification);
}