package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Integer status;
    private String statusText;
    private Integer paymentStatus;
    private String paymentStatusText;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private Integer itemsCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private ShippingAddressResponse shippingAddress;

}
