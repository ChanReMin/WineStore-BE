package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogListResponse {
    private List<InventoryLogResponse> logs;
    private PaginationInfo pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer currentPage;

        private Integer totalPages;

        private Long totalItems;

        private Integer perPage;
    }
}
