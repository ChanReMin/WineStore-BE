package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class UnBanWarehouseResponse {
    private Long id;
    private String name;
    private Integer status;
    private Long managerId;
    private LocalDateTime updatedAt;
}
