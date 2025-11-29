package com.example.demo.services.commands;

import com.example.demo.dtos.responses.brand.BrandListItemResponse;
import com.example.demo.entities.Brand;

public interface BrandCommandService {
    BrandListItemResponse createBrand(Brand brand);
}
