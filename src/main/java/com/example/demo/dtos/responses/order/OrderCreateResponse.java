package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class OrderCreateResponse {
    private Long orderId;
    private String orderCode;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private Integer status; // Corresponds to OrderStatus enum value
    private String statusText;
    private Integer paymentStatus; // Corresponds to PaymentStatus enum value
    private String paymentStatusText;
    private String paymentUrl;
    private OffsetDateTime createdAt;

}
