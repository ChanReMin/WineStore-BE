package com.example.demo.services.queries;

import com.example.demo.commons.annotations.ReadOnlyService;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.ProductListResponse;
import com.example.demo.dtos.responses.product.ProductResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.configs.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AccountRepository accountRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;

    @ReadOnlyService
    public ProductResponse getAllProducts(Integer page, Integer limit, Integer status, String search) {
        log.info("Fetching products - page: {}, limit: {}, status: {}, search: {}", page, limit, status, search);

        // Get current user email and check role
        String currentUserEmail = securityUtils.getCurrentUserEmail();
        boolean isSeller = hasRole("SELLER");
        boolean isAdmin = hasRole("ADMIN");

        log.info("Current user: {}, isSeller: {}, isAdmin: {}", currentUserEmail, isSeller, isAdmin);

        // Create pageable with sorting by createdAt DESC
        Pageable pageable = PageRequest.of(
                page - 1, // Spring Data JPA uses 0-based indexing
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Convert status code to enum
        ProductStatus productStatus = status != null ? ProductStatus.fromCode(status) : null;

        Page<Product> productPage;

        // Sellers can only view their own products.
        if (isSeller && !isAdmin) {
            Account currentUser = accountRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            productPage = productRepository.findAllByCreatedByWithFilters(
                    currentUser, productStatus, search, pageable);

            log.info("Filtered products for seller: {}", currentUserEmail);
        }
        // ADMIN see all product
        else {
            productPage = productRepository.findAllWithFilters(productStatus, search, pageable);
            log.info("Fetching all products for admin");
        }

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
        Long totalCount, pendingCount, activeCount, bannedCount;

        if (isSeller && !isAdmin) {
            // Count only seller's products
            Account currentUser = accountRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            totalCount = productRepository.countByCreatedBy(currentUser);
            pendingCount = productRepository.countByCreatedByAndStatus(currentUser, ProductStatus.PENDING);
            activeCount = productRepository.countByCreatedByAndStatus(currentUser, ProductStatus.ACTIVE);
            bannedCount = productRepository.countByCreatedByAndStatus(currentUser, ProductStatus.BAN);
        } else {
            // Count all products for admin
            totalCount = productRepository.countAllActive();
            pendingCount = productRepository.countByStatus(ProductStatus.PENDING);
            activeCount = productRepository.countByStatus(ProductStatus.ACTIVE);
            bannedCount = productRepository.countByStatus(ProductStatus.BAN);
        }

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

    /**
     * Helper method to check if current user has a specific role
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
    }
}