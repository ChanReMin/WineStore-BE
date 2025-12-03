package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SellerOrderItemDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private BigDecimal lineTotal;
    private BigDecimal profit;
    private Long warehouseId;
}
