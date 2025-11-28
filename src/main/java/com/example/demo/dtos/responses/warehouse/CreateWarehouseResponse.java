package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateWarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status;
    private String city;
    private Long managerId;
    private LocalDateTime createdAt;
}
