package com.example.demo.controllers;

import com.example.demo.dtos.commands.brand.BrandCreateRequest;
import com.example.demo.dtos.responses.brand.BrandResponse;
import com.example.demo.entities.Brand;
import com.example.demo.services.commands.BrandCommandService;
import com.example.demo.services.queries.BrandQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Inventory Management")
public class BrandController {
    private final BrandCommandService brandCommandService;
    private final BrandQueryService brandQueryService;

    @GetMapping
    public List<BrandResponse> getAllBrands() {
        return brandQueryService.getAllBrands();
//                .stream()
//                .map(b -> new BrandResponse(b.getId(), b.getName(), b.getCountry(), b.getDescription()))
//                .toList();
    }

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@RequestBody BrandCreateRequest request) {

        Brand brand = Brand.builder()
                .name(request.getName())
                .country(request.getCountry())
                .description(request.getDescription())
                .build();

        BrandResponse response = brandCommandService.createBrand(brand);

        return ResponseEntity.ok(response);
    }
}
