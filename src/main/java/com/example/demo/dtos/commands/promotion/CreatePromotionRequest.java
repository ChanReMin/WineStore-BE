package com.example.demo.dtos.commands.promotion;

import com.example.demo.commons.enums.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePromotionRequest {

    @NotBlank(message = "Promotion code is required")
    @Size(min = 6, max = 20, message = "Promotion code must be between 6 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Promotion code must contain only uppercase letters and numbers")
    private String code;

    @NotBlank(message = "Promotion name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discount_type; // Use enum for type validation

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discount_value;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be current or in the future")
    private LocalDate start_date;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate end_date;

    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer max_usage;

    @Min(value = 0, message = "Invalid status")
    @Max(value = 1, message = "Invalid status")
    private Integer status; // 0: Inactive, 1: Active

    private List<Long> product_ids; // IDs of products to apply promotion to
    private List<Long> category_ids; // IDs of categories to apply promotion to (all products in these categories)
    private List<Long> excluded_product_ids; // IDs of products to exclude from promotion

    @Valid
    private PromotionConditionsRequest conditions; // Nested conditions object
}
