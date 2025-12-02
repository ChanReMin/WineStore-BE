package com.example.demo.services;

import com.example.demo.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentServiceFactory {

    private final VnpayService vnpayService;
    private final MomoService momoService;

    public PaymentService getPaymentService(String paymentMethod) {
        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            return vnpayService;
        }
        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            return momoService;
        }
        throw new BadRequestException("Unsupported payment method: " + paymentMethod);
    }
}
