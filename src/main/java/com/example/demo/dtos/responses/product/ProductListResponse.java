package com.example.demo.dtos.responses.product;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String category;
    private String brand;
    private Integer status;
    private String statusText;
    private Integer totalInventory;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
}
