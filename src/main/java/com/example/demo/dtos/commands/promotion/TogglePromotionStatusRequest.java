package com.example.demo.dtos.commands.promotion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TogglePromotionStatusRequest {
    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Invalid status value")
    @Max(value = 1, message = "Invalid status value")
    private Integer status; // 0: Inactive, 1: Active
}
