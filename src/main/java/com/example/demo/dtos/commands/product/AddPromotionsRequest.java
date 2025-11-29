package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPromotionsRequest {

    @NotEmpty(message = "Promotion IDs cannot be empty")
    private List<Long> promotionIds;
}