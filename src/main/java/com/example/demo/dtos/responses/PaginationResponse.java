package com.example.demo.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaginationResponse {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int perPage;
}
