package com.example.demo.dtos.responses.warehouse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RejectWarehouseResponse {
    private Long id;
    private String name;
    private Integer status;
    private Boolean deleted;
    private String rejectionNote;
}
