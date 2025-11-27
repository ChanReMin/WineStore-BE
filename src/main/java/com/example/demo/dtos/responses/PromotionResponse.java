package com.example.demo.dtos.responses;

import com.example.demo.commons.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PromotionResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer discountType; // Changed to Integer to match JSON spec
    private String discountTypeText;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer usedCount;
    private Integer remainingUsage;
    private Integer status; // Changed to Integer to match JSON spec
    private String statusText;
    private BigDecimal minimumOrderValue;
    private Integer applicableProductsCount;
    private LocalDateTime createdAt;
}
