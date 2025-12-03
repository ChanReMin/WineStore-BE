package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSegmentDTO {
    private Long count;

    private BigDecimal totalSpent;

    private BigDecimal averageOrderValue;
}
