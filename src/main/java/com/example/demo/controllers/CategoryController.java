package com.example.demo.controllers;

import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.category.CategoryListItemResponse;
import com.example.demo.dtos.responses.category.CategoryListResponse;
import com.example.demo.services.queries.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;


    @GetMapping
    public ResponseEntity<SuccessResponse<CategoryListResponse>> getAllBrands() {

        List<CategoryListItemResponse> categories = categoryQueryService.getAllCategories();

        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categories)
                .build();

        return ResponseEntity.ok(
                SuccessResponse.<CategoryListResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }
}