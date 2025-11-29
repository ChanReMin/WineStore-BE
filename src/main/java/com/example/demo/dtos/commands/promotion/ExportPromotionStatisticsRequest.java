package com.example.demo.dtos.commands.promotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportPromotionStatisticsRequest {
    @NotBlank(message = "Format cannot be blank")
    private String format; // e.g., "xlsx", "csv"
    @NotNull(message = "Include customer details cannot be null")
    private Boolean include_customer_details;

}
