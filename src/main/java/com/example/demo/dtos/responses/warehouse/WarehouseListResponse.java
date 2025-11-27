package com.example.demo.dtos.responses.warehouse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseListResponse {
    private List<WarehouseResponse> warehouses;

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