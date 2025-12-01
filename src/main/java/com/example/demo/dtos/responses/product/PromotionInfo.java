package com.example.demo.dtos.responses.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude()
public class PromotionInfo {
    private Long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
}
