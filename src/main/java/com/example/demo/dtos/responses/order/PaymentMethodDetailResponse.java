package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodDetailResponse {
    private Integer id;
    private String name;
    private String code;
}
