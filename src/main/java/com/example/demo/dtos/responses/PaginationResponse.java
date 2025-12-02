package com.example.demo.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponse {
    private Integer currentPage;
    private Integer totalPages;
    private Long totalItems;
    private Integer perPage;

}
