package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateWarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer status;
    private LocalDateTime updatedAt;
}
