package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDetailResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private long totalOrders;
}
