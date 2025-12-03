package com.example.demo.dtos.responses.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsDTO {
    private Long total;
    private Long active;

    private Long pendingApproval;

    private Long rejected;

    private Long outOfStock;
}
