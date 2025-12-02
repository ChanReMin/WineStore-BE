package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponDetailResponse {
    private String code;
    private Integer discountType; // 1=%, 2=fixed
    private BigDecimal discountValue;
    private BigDecimal discountAmount;

}
