package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class WarehouseSellerListResponse {
    private List<WarehouseSellerResponse> warehouses;

    private Pagination pagination;

    private Summary summary;

    @Getter
    @Setter
    @Builder
    public static class Pagination {
        private Integer currentPage;
        private Integer totalPages;
        private Long totalItems;
        private Integer perPage;
        private Boolean hasNext;
        private Boolean hasPrev;
    }

    @Getter
    @Setter
    @Builder
    public static class Summary {
        private Long totalWarehouses;
        private Long active;
        private Long pending;
        private Long banned;
    }
}
