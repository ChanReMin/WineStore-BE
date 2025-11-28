package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${app.ai.endpoint}")
    private String aiServiceUrl;

    public void sendProductToAI(Long productId) {
        try {
            restTemplate.postForObject(
                    aiServiceUrl,
                    Map.of("productId", productId),
                    Void.class
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send product to AI service", ex);
        }
    }
}
