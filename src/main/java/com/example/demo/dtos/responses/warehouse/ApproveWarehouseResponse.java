package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class ApproveWarehouseResponse {
    private Long id;
    private String name;
    private String location;
    private Integer status;
    private Long managerId;
    private LocalDateTime updateAt;
}
