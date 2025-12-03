package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSummaryResponse {
    private long pending;
    private long processing;
    private long shipping;
    private long completed;
    private long cancelled;
}
