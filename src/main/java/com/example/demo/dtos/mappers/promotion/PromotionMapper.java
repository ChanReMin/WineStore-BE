package com.example.demo.dtos.mappers.promotion;

import com.example.demo.commons.enums.DiscountType;
import com.example.demo.dtos.responses.promotion.*;
import com.example.demo.entities.Category;
import com.example.demo.entities.Promotion;
import com.example.demo.entities.PromotionProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PromotionMapper {

    public PromotionResponse toPromotionResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        String discountTypeText;
        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discountTypeText = "Phần trăm";
        } else if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountTypeText = "Số tiền cố định";
        } else {
            discountTypeText = "Không xác định";
        }

        String statusText;
        if (promotion.getStatus() == 1) { // Assuming 1 for active
            statusText = "Active";
        } else {
            statusText = "Inactive";
        }

        // Calculate remaining usage
        int remainingUsage = 0;
        if (promotion.getMaxUsage() != null && promotion.getUsedCount() != null) {
            remainingUsage = promotion.getMaxUsage() - promotion.getUsedCount();
        }


        return PromotionResponse.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discount_type(promotion.getDiscountType().ordinal()) // Use ordinal() for enum int value
                .discount_type_text(discountTypeText)
                .discount_value(promotion.getDiscountValue().doubleValue())
                .start_date(promotion.getStartDate())
                .end_date(promotion.getEndDate())
                .max_usage(promotion.getMaxUsage() != null ? promotion.getMaxUsage() : 0)
                .used_count(promotion.getUsedCount() != null ? promotion.getUsedCount() : 0)
                .remaining_usage(remainingUsage)
                .status(promotion.getStatus() != null ? promotion.getStatus() : 0)
                .status_text(statusText)
                // For applicable_products_count, we don't have it in the entity, so setting to 0 for now.
                // This would require a join or separate query.
                .applicable_products_count(0)
                .created_at(promotion.getCreatedAt())
                .build();
    }

    public PromotionDetailResponse toPromotionDetailResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        String discountTypeText;
        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discountTypeText = "Phần trăm";
        } else if (promotion.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountTypeText = "Số tiền cố định";
        } else {
            discountTypeText = "Không xác định";
        }

        String statusText;
        if (promotion.getStatus() == 1) { // Assuming 1 for active
            statusText = "Active";
        } else {
            statusText = "Inactive";
        }

        // Calculate remaining usage
        int remainingUsage = 0;
        if (promotion.getMaxUsage() != null && promotion.getUsedCount() != null) {
            remainingUsage = promotion.getMaxUsage() - promotion.getUsedCount();
        }

        // Map applicable products
        List<PromotionProductDetailResponse> applicableProducts = promotion.getPromotionProducts().stream()
                .map(promoProduct -> PromotionProductDetailResponse.builder()
                        .id(promoProduct.getProduct().getId())
                        .name(promoProduct.getProduct().getName())
                        .price(promoProduct.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        // Derive applicable categories from applicable products
        Set<PromotionCategoryResponse> uniqueCategories = promotion.getPromotionProducts().stream()
                .map(promoProduct -> promoProduct.getProduct().getCategory())
                .map(category -> PromotionCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .collect(Collectors.toSet());
        List<PromotionCategoryResponse> applicableCategories = new ArrayList<>(uniqueCategories);


        return PromotionDetailResponse.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discount_type(promotion.getDiscountType().ordinal())
                .discount_type_text(discountTypeText)
                .discount_value(promotion.getDiscountValue())
                .max_discount_amount(null) // Not directly in entity, assuming default or null for now
                .minimum_order_value(null) // Not directly in entity, assuming default or null for now
                .start_date(promotion.getStartDate())
                .end_date(promotion.getEndDate())
                .max_usage(promotion.getMaxUsage())
                .max_usage_per_customer(null) // Not directly in entity, assuming default or null for now
                .used_count(promotion.getUsedCount())
                .remaining_usage(remainingUsage)
                .status(promotion.getStatus())
                .status_text(statusText)
                .applicable_products(applicableProducts)
                .applicable_categories(applicableCategories)
                .excluded_products(new ArrayList<>()) // Not directly in entity, assuming empty for now
                .conditions(PromotionConditionsResponse.builder() // Not directly in entity, assuming defaults
                        .minimum_quantity(null)
                        .apply_to("all") // Default or logic to derive
                        .stackable(false)
                        .first_order_only(false)
                        .build())
                .created_at(promotion.getCreatedAt())
                .updated_at(promotion.getUpdatedAt()) // Assuming BaseEntity provides this
                .build();
    }

    public CreatePromotionResponse toCreatePromotionResponse(Promotion savedPromotion) {
        if (savedPromotion == null) {
            return null;
        }
        return CreatePromotionResponse.builder()
                .id(savedPromotion.getId())
                .code(savedPromotion.getCode())
                .name(savedPromotion.getName())
                .status(savedPromotion.getStatus())
                .created_at(savedPromotion.getCreatedAt())
                .build();
    }

    public UpdatePromotionResponse toUpdatePromotionResponse(Promotion updatedPromotion) {
        if (updatedPromotion == null) {
            return null;
        }
        return UpdatePromotionResponse.builder()
                .id(updatedPromotion.getId())
                .code(updatedPromotion.getCode())
                .updated_at(updatedPromotion.getUpdatedAt())
                .build();
    }

    public TogglePromotionStatusResponse toTogglePromotionStatusResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        String statusText;
        if (promotion.getStatus() == 1) {
            statusText = "Active";
        } else {
            statusText = "Inactive";
        }

        return TogglePromotionStatusResponse.builder()
                .id(promotion.getId())
                .status(promotion.getStatus())
                .status_text(statusText)
                .build();
    }
}
