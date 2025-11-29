package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionListResponse {
    private List<PromotionResponse> promotions;
    private PaginationInfo pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private int current_page;
        private int total_pages;
        private long total_items;
        private int per_page;
    }
}
