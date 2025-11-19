package com.example.demo.models.mappers;

import com.example.demo.entities.Brand;
import com.example.demo.models.dtos.responses.BrandResponse;

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
