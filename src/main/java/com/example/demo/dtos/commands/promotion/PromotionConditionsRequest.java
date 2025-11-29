package com.example.demo.dtos.commands.promotion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionConditionsRequest {
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    private Integer minimum_quantity;
    @NotBlank(message = "Apply condition is required")
    private String apply_to; // e.g., "all", "specific"
    private Boolean stackable;
    private Boolean first_order_only;
}
