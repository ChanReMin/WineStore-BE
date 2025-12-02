package com.example.demo.dtos.responses.order;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class OrderTimelineEventResponse {
    private Integer status;
    private String statusText;
    private OffsetDateTime timestamp;
    private String note;

}
