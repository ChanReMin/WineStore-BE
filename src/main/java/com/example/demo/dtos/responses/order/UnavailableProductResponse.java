package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnavailableProductResponse {
    private Long productId;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableQuantity;

}
