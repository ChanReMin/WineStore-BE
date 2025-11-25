package com.example.demo.dtos.responses.product;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
    private List<Object> products;
    private PaginationInfo pagination;
    private ProductSummary summary;

    @Getter
    @Setter
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalItems;
        private Integer perPage;
    }

    @Getter
    @Setter
    @Builder
    public static class ProductSummary {
        private Long total;
        private Long pending;
        private Long active;
        private Long banned;
    }
}
