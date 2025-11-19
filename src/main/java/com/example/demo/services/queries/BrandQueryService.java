package com.example.demo.services.queries;

import com.example.demo.commons.annotations.ReadOnlyService;
import com.example.demo.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandQueryService {
    private final BrandRepository brandRepository;

//    @ReadOnlyService
    public List<BrandResponse> getAllBrands() {
        // Use a custom query that only selects the fields you need
        return brandRepository.findAllBrandsWithRequiredFields();
    }
}
