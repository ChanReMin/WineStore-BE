package com.example.demo.dtos.responses.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPromotionsResponse {

    private Long productId;
    private List<PromotionInfo> addedPromotions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionInfo {
        private Long id;
        private String code;
        private String name;
    }
}