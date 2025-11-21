package com.example.demo.dtos.mappers;

import com.example.demo.entities.Brand;
import com.example.demo.dtos.responses.brand.BrandResponse;

public class BrandMapper {

    public static BrandResponse toResponse(Brand brand) {
        if (brand == null) return null;

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .country(brand.getCountry())
                .description(brand.getDescription())
                .build();
    }
}
