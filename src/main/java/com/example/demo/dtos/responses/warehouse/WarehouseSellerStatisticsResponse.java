package com.example.demo.dtos.responses.warehouse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseSellerStatisticsResponse {

    private Long totalWarehouses;

    private Long activeWarehouses;

    private Long pendingWarehouses;

    private Long bannedWarehouses;

    private Long totalInventoryValue;

    private Integer totalProducts;

    private Integer totalQuantity;

    private List<StatusCount> warehousesByStatus;

    @Getter
    @Setter
    @Builder
    public static class StatusCount {
        private Integer status;
        private Long count;
        private String label;
    }
}
