package com.example.demo.dtos.responses.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDTO {
    private LocalDate startDate;

    private LocalDate endDate;
}
