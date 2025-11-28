package com.example.demo.dtos.mappers;

import com.example.demo.entities.Brand;
import com.example.demo.dtos.responses.brand.BrandListItemResponse;

public class BrandMapper {

    public static BrandListItemResponse toResponse(Brand brand) {
        if (brand == null) return null;

        return BrandListItemResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .country(brand.getCountry())
                .build();
    }
}
