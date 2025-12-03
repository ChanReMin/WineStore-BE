package com.example.demo.services.notifications;

import com.example.demo.dtos.notifications.NotificationMessage;
import com.example.demo.dtos.responses.notification.NotificationResponse;
import com.example.demo.services.commands.NotificationCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final NotificationCommandService notificationCommandService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    @Value("${application.notification.queue-url}")
    private String queueUrl;

    @Scheduled(fixedDelay = 2000)
    public void pollQueue() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(1)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {
            try {
                NotificationMessage payload =
                        objectMapper.readValue(message.body(), NotificationMessage.class);

                NotificationResponse savedNotification =
                        notificationCommandService.saveNotification(payload);

                String userId = payload.getUserId().toString();
                String destination = "/queue/notifications";
                messagingTemplate.convertAndSendToUser(
                        userId,
                        destination,
                        savedNotification
                );

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
            }
        }
    }
}