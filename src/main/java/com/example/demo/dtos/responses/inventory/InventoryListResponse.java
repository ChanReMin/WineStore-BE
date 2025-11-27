package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

// ============= Inventory List Response =============
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryListResponse {
    private List<InventoryItemResponse> inventory;
    private PaginationInfo pagination;
    private SummaryInfo summary;

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
    public static class SummaryInfo {
        private Long totalProducts;

        private Long inStock;

        private Long lowStock;

        private Long outOfStock;

        private BigDecimal totalValue;
    }
}
