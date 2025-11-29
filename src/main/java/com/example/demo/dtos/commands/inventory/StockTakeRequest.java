package com.example.demo.dtos.commands.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTakeRequest {
    private Long warehouseId;

    @NotEmpty(message = "Items IDs cannot be empty")
    private List<StockTakeItem> items;
    private String note;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockTakeItem {
        private Long productId;

        private Integer systemQuantity;

        private Integer actualQuantity;

        private String note;
    }
}
