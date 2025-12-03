package com.example.demo.dtos.responses.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellerInventoryAlertProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}
