package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPerformanceDTO {
    private Long categoryId;

    private String categoryName;

    private BigDecimal revenue;
    private Long orders;
    private BigDecimal percentage;
}
