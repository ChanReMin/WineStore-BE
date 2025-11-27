package com.example.demo.dtos.responses.warehouse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status; // 0, 1, 2

    private ManagerInfo manager;

    private InventorySummary inventorySummary;

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
        private Integer outOfStockProducts;
    }
}