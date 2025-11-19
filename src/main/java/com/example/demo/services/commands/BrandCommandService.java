package com.example.demo.services.commands;

import com.example.demo.commons.annotations.ReadOnlyService;
import com.example.demo.entities.Brand;
import com.example.demo.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BrandCommandService {
    private final BrandRepository brandRepository;

    @ReadOnlyService
    public BrandResponse createBrand(Brand brand) {
        Brand savedBrand = brandRepository.save(brand);
        return BrandMapper.toResponse(savedBrand);
    }

}
