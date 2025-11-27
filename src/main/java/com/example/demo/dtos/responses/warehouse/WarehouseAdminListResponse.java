package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WarehouseAdminListResponse {

    private List<WarehouseAdminItemResponse> requests;

    private Pagination pagination;

    @Data
    @Builder
    public static class Pagination {
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private int perPage;
        private boolean hasNext;
        private boolean hasPrev;
    }
}
