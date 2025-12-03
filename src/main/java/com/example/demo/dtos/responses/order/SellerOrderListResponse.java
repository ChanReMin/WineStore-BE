package com.example.demo.dtos.responses.order;

import com.example.demo.dtos.responses.PaginationResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SellerOrderListResponse {
    private List<SellerOrderListItemResponse> orders;
    private PaginationResponse pagination;
    private OrderSummaryResponse summary;
}
