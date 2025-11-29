package com.example.demo.dtos.responses.promotion;

import com.example.demo.commons.enums.DiscountType;
import com.example.demo.dtos.responses.product.ProductResponse; // Assuming ProductResponse is suitable for applicable_products
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDetailResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private int discount_type;
    private String discount_type_text;
    private BigDecimal discount_value;
    private BigDecimal max_discount_amount; // Not in entity, will be null or default
    private BigDecimal minimum_order_value; // Not in entity, will be null or default
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private Integer max_usage;
    private Integer max_usage_per_customer; // Not in entity, will be null or default
    private Integer used_count;
    private Integer remaining_usage;
    private Integer status;
    private String status_text;
    private List<PromotionProductDetailResponse> applicable_products; // Custom DTO for products in promotion
    private List<PromotionCategoryResponse> applicable_categories; // Derived or default
    private List<PromotionProductDetailResponse> excluded_products; // Not in entity, will be empty or default
    private PromotionConditionsResponse conditions; // Not in entity, will be default
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
