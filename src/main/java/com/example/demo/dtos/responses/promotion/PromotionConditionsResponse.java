package com.example.demo.dtos.responses.promotion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionConditionsResponse {
    private Integer minimum_quantity; // Not in entity
    private String apply_to; // Not in entity (e.g., "all", "selected_products", "selected_categories")
    private Boolean stackable; // Not in entity
    private Boolean first_order_only; // Not in entity
}
