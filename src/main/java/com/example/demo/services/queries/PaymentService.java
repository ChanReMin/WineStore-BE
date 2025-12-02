package com.example.demo.services.queries;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    String createPaymentUrl(HttpServletRequest request, Long orderId, long amount);
}
