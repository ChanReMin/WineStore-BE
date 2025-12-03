package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStatsDTO {
    private BigDecimal total;
    private BigDecimal percentage;
    private Long orders;
}
