package com.example.demo.dtos.responses.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerInventoryAlertWarehouseResponse {
    private Long id;
    private String name;
    private String location;
}
