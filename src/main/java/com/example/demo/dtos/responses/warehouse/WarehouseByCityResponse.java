package com.example.demo.dtos.responses.warehouse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseByCityResponse {

    private List<WarehouseItem> data;
    private Long total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseItem {
        private Long id;
        private String name;
        private String location;
        private String description;
        private Integer status;
        private String city;

        @JsonProperty("managerId")
        private Long managerId;

        @JsonProperty("createdAt")
        private LocalDateTime createdAt;
    }
}
