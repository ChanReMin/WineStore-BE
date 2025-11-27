package com.example.demo.dtos.responses.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTakeResponse {
    private Long stockTakeId;

    private Long warehouseId;

    private Integer totalItems;

    private Integer itemsAdjusted;

    private List<AdjustmentInfo> adjustments;

    private LocalDateTime createdAt;

    @Getter
    @Setter
    @Builder
    public static class AdjustmentInfo {
        private Long productId;

        private Integer difference;
        private String note;
    }
}