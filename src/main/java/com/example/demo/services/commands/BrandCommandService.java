package com.example.demo.services.commands;

import com.example.demo.commons.annotations.WriteService;
import com.example.demo.entities.Brand;
import com.example.demo.dtos.responses.brand.BrandResponse;
import com.example.demo.dtos.mappers.BrandMapper;
import com.example.demo.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BrandCommandService {
    private final BrandRepository brandRepository;

    @WriteService
    public BrandResponse createBrand(Brand brand) {
        Brand savedBrand = brandRepository.save(brand);
        return BrandMapper.toResponse(savedBrand);
    }

}
