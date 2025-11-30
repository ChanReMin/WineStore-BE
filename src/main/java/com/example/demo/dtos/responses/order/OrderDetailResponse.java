package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {
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
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime shippedAt;
    private String note;
    private List<OrderItemDetailResponse> items;
    private ShippingAddressDetailResponse shippingAddress;
    private PaymentMethodDetailResponse paymentMethod;
    private CouponDetailResponse coupon; // Renamed from promotion for clarity
    private ShippingInfoResponse shippingInfo;
    private List<OrderTimelineEventResponse> timeline;
}
