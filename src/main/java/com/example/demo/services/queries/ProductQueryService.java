package com.example.demo.services.queries;

import com.example.demo.commons.annotations.ReadOnlyService;
import com.example.demo.commons.enums.ProductStatus;

import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.ProductListResponse;
import com.example.demo.dtos.responses.product.ProductResponse;
import com.example.demo.entities.Product;
import com.example.demo.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @ReadOnlyService
    public ProductResponse getAllProducts(Integer page, Integer limit, Integer status, String search) {
        log.info("Fetching products - page: {}, limit: {}, status: {}, search: {}", page, limit, status, search);

        // Create pageable with sorting by createdAt DESC
        Pageable pageable = PageRequest.of(
                page - 1, // Spring Data JPA uses 0-based indexing
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Convert status code to enum
        ProductStatus productStatus = status != null ? ProductStatus.fromCode(status) : null;

        // Fetch products with filters using JOIN FETCH to avoid N+1
        Page<Product> productPage = productRepository.findAllWithFilters(productStatus, search, pageable);

        // Map to response
        List<ProductListResponse> products = productPage.getContent().stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());

        // Build pagination info
        ProductResponse.PaginationInfo pagination = ProductResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .build();

        // Build summary (count by status)
        Long totalCount = productRepository.countAllActive();
        Long pendingCount = productRepository.countByStatus(ProductStatus.PENDING);
        Long activeCount = productRepository.countByStatus(ProductStatus.ACTIVE);
        Long bannedCount = productRepository.countByStatus(ProductStatus.BAN);

        ProductResponse.ProductSummary summary = ProductResponse.ProductSummary.builder()
                .total(totalCount)
                .pending(pendingCount)
                .active(activeCount)
                .banned(bannedCount)
                .build();

        // Build final response
        return ProductResponse.builder()
                .products(products)
                .pagination(pagination)
                .summary(summary)
                .build();
    }
}