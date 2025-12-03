package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSummaryResponse {
    private long pending;
    private long confirmed;
    private long paid;
    private long cancelled;
}

