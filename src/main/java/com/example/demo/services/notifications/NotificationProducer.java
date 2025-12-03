package com.example.demo.services.notifications;

import com.example.demo.dtos.notifications.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${application.notification.queue-url}")
    private String queueUrl;

    public void send(NotificationMessage payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build();

            sqsClient.sendMessage(request);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notification payload", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to SQS", e);
        }
    }
}