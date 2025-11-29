package com.example.demo.services.queries;

import com.example.demo.dtos.responses.brand.BrandListItemResponse;

import java.util.List;

public interface BrandQueryService {
    List<BrandListItemResponse> getAllBrands();
}
