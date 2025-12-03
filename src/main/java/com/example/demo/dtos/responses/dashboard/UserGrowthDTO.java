package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthDTO {
    private LocalDate date;

    private Long newUsers;

    private Long totalUsers;
}
