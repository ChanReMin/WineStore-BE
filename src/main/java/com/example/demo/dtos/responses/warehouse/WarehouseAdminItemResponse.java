package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseAdminItemResponse {

    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status;

    private ManagerInfo manager;

    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    public static class ManagerInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
    }
}
