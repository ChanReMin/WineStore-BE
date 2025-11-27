package com.example.demo.dtos.responses.warehouse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// Response chung cho cả Admin và Seller
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseDetailResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status;

    // Chỉ có Admin mới có field này
    private ManagerInfo manager;

    // Chỉ có Seller mới có field này
    private Long managerId;

    private InventorySummary inventory;

    private Statistics statistics;

    private List<RecentLog> recentLogs;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ManagerInfo {
        private Long id;
        private Long accountId;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private Integer role;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    public static class InventorySummary {
        private Integer totalProducts;
        private Integer totalQuantity;
        private Long totalValue;
        private Integer lowStockProducts;

        // Admin có thêm field này
        private Integer outOfStockProducts;
    }

    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Statistics {
        private Integer totalOrdersFromThisWarehouse;
        private Long totalRevenue;
        private Integer last30DaysOrders;
    }

    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RecentLog {
        private Long id;
        private String type;
        private String productName;
        private Integer quantity;

        // Admin có thêm các field này
        private String userName;
        private Long referenceId;
        private String referenceType;

        private LocalDateTime createdAt;
    }
}