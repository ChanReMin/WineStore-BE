package com.example.demo.services.queries;

import com.example.demo.dtos.responses.category.CategoryListItemResponse;

import java.util.List;

public interface CategoryQueryService {

    List<CategoryListItemResponse> getAllCategories();
}
