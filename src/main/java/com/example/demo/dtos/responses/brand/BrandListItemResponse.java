package com.example.demo.dtos.responses.brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandListItemResponse {
    private Long id;
    private String name;
    private String country;
    private Long productsCount;
}
