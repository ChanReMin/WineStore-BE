package com.example.demo.dtos.commands.order;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class OrderCreateRequest {
    @NotNull(message = "Shipping address ID cannot be null")
    private Long shippingAddressId;

    @NotNull(message = "Payment method ID cannot be null")
    private Integer paymentMethodId;

    private String note;
    private String couponCode;
}
