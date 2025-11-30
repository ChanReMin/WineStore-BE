package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class ShippingAddressResponse {
    private String fullName;
    private String phoneNumber;
    private String addressLine;
    private String city;
}
