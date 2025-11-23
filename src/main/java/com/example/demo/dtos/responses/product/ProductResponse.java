package com.example.demo.dtos.responses.product;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private List<ProductListResponse> products;
    private PaginationInfo pagination;
    private ProductSummary summary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSummary {
        private Long total;
        private Long pending;
        private Long active;
        private Long banned;
    }
}

