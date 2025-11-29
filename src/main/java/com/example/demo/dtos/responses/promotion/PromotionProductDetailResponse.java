package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionProductDetailResponse {
    private Long id;
    private String name;
    private BigDecimal price;
}
