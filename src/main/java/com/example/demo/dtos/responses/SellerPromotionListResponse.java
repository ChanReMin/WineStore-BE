package com.example.demo.dtos.responses;

import com.example.demo.dtos.responses.PaginationResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SellerPromotionListResponse {
    private boolean success;
    private Data data;

    @Getter
    @Setter
    @Builder
    public static class Data {
        private List<PromotionResponse> promotions;
        private PaginationResponse pagination;
    }
}
