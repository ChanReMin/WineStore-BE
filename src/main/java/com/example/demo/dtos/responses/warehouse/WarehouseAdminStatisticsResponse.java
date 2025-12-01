package com.example.demo.dtos.responses.warehouse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseAdminStatisticsResponse {

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

        private Long approveWarehouses;

        private Long pendingWarehouses;

        private Long rejectWarehouses;

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