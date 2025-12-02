package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingAddressDetailResponse {
    private String fullName;
    private String phoneNumber;
    private String addressLine;
    private String city;
    private String state;
    private String country;

}
