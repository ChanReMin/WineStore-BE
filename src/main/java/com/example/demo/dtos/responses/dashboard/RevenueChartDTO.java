package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {
    private LocalDate date;
    private BigDecimal revenue;
    private Long orders;
    private BigDecimal profit;
    private Long customers;
}
