package com.example.demo.controllers;

import com.example.demo.dtos.commands.brand.BrandCreateRequest;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.brand.BrandListResponse;
import com.example.demo.dtos.responses.brand.BrandListItemResponse;
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
@Tag(name = "Brands Management")
public class BrandController {
    private final BrandCommandService brandCommandService;
    private final BrandQueryService brandQueryService;

    @GetMapping
    public ResponseEntity<SuccessResponse<BrandListResponse>> getAllBrands() {

        List<BrandListItemResponse> brands = brandQueryService.getAllBrands();

        BrandListResponse response = BrandListResponse.builder()
                .brands(brands)
                .build();

        return ResponseEntity.ok(
                SuccessResponse.<BrandListResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }


    @PostMapping
    public ResponseEntity<BrandListItemResponse> createBrand(@RequestBody BrandCreateRequest request) {

        Brand brand = Brand.builder()
                .name(request.getName())
                .country(request.getCountry())
                .description(request.getDescription())
                .build();

        BrandListItemResponse response = brandCommandService.createBrand(brand);

        return ResponseEntity.ok(response);
    }
}
