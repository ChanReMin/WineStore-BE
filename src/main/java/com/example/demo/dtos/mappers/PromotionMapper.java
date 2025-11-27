package com.example.demo.dtos.mappers;

import com.example.demo.commons.enums.DiscountType;
import com.example.demo.entities.Promotion;
import com.example.demo.dtos.responses.PromotionResponse;

import java.time.LocalDateTime;

public class PromotionMapper {

    public static PromotionResponse toResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        Integer remainingUsage = null;
        if (promotion.getMaxUsage() != null && promotion.getUsedCount() != null) {
            remainingUsage = promotion.getMaxUsage() - promotion.getUsedCount();
        }

        String discountTypeText = null;
        if (promotion.getDiscountType() != null) {
            switch (promotion.getDiscountType()) {
                case PERCENTAGE: discountTypeText = "Phần trăm"; break;
                case FIXED_AMOUNT: discountTypeText = "Số tiền cố định"; break;
            }
        }

        String statusText = null;
        if (promotion.getStatus() != null) {
            // 0: Inactive, 1: Active
            statusText = promotion.getStatus() == 1 ? "Active" : "Inactive";
        }


        return PromotionResponse.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType() != null ? promotion.getDiscountType().getValue() : null) // Using getValue()
                .discountTypeText(discountTypeText)
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .maxUsage(promotion.getMaxUsage())
                .usedCount(promotion.getUsedCount())
                .remainingUsage(remainingUsage)
                .status(promotion.getStatus())
                .statusText(statusText)
                .minimumOrderValue(promotion.getMinimumOrderValue())
                .applicableProductsCount(promotion.getPromotionProducts() != null ? promotion.getPromotionProducts().size() : 0)
                .createdAt(promotion.getCreatedAt())
                .build();
    }
}
