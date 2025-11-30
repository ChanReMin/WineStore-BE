package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ShippingInfoResponse {
    private String carrier;
    private String trackingNumber;
    private OffsetDateTime estimatedDelivery;
}
