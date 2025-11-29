package com.example.demo.services.queries;

import com.example.demo.dtos.responses.product.ProductListResponse;

import java.math.BigDecimal;

public interface ProductQueryService {

    ProductListResponse getAllProducts(
            Integer page,
            Integer limit,
            String search,
            Integer status,
            Long categoryId,
            Long brandId,
            Long warehouseId,
            BigDecimal priceFrom,
            BigDecimal priceTo,
            BigDecimal concentrationFrom,
            BigDecimal concentrationTo);

    Object getProductById(Long productId);

    Object getRelatedProducts(Long productId);
}
