package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class OrderCancelResponse {
    private Long orderId;
    private Integer status;
    private String statusText;
    private OffsetDateTime cancelledAt;
    private String refundStatus;
    private OffsetDateTime estimatedRefundDate;
}
