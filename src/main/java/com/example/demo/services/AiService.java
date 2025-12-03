package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${app.ai.endpoint}")
    private String baseUrl;

    @Value("${app.ai.product-endpoint}")
    private String productPath;

    public void sendProductToAI(Long productId) {
        String fullUrl = baseUrl + productPath;
        restTemplate.postForObject(
                fullUrl,
                Map.of("id", productId),
                Void.class
        );
    }

    public void updateProductInAI(Long productId) {
        String fullUrl = baseUrl + productPath;

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(Map.of("id", productId));

        restTemplate.exchange(
                fullUrl,
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }
}
