package com.example.demo.dtos.responses.warehouse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseStatisticsResponse {
    private Overview overview;
    private Inventory inventory;
    private List<StatusBreakdown> byStatus;
    private List<TopWarehouse> topWarehouses;
    private List<RecentRequest> recentRequests;

    @Getter
    @Setter
    @Builder
    public static class Overview {
        private Long totalWarehouses;
        private Long activeWarehouses;
        private Long pendingWarehouses;
        private Long bannedWarehouses;
    }

    @Getter
    @Setter
    @Builder
    public static class Inventory {
        private Integer totalProducts;
        private Integer totalQuantity;
        private Long totalInventoryValue;
    }

    @Getter
    @Setter
    @Builder
    public static class StatusBreakdown {
        private Integer status;
        private String statusLabel;
        private Long count;
        private Double percentage;
    }

    @Getter
    @Setter
    @Builder
    public static class TopWarehouse {
        private Long id;
        private String name;
        private String managerName;
        private Long totalValue;
        private Integer totalProducts;
    }

    @Getter
    @Setter
    @Builder
    public static class RecentRequest {
        private Long id;
        private String name;
        private String managerName;
        private Integer status;
        private LocalDateTime createdAt;
    }
}