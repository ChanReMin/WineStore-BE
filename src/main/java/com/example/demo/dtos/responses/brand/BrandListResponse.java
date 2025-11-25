package com.example.demo.dtos.responses.brand;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandListResponse {
    private List<BrandListItemResponse> brands;
}