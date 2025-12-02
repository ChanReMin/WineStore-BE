package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.entities.Brand;
import com.example.demo.repositories.queries.*;
import com.example.demo.services.queries.ProductQueryService;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // Added import for Optional
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductQueryRepository productQueryRepository;
    private final AccountQueryRepository accountRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;
    private final BrandQueryRepository brandQueryRepository;
    private final CategoryQueryRepository categoryQueryRepository;
    private final WarehouseQueryRepository warehouseQueryRepository;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public ProductListResponse getAllProducts(
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
            BigDecimal concentrationTo) {

        log.info("📋 Fetching products - page: {}, limit: {}, search: {}, status: {}",
                page, limit, search, status);

        if (page != null && page < 0) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (limit != null && limit < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        // Set defaults
        page = (page != null && page > 0) ? page : 1;
        limit = (limit != null && limit > 0) ? Math.min(limit, 100) : 10;

        // Create pageable with default sorting (newest first)
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Determine if seller view (authenticated seller) or customer view
        boolean isSellerView = isAuthenticated() && (hasRole("SELLER") || hasRole("ADMIN"));

        if(status != null && (status < 0 || status > 2)) {
            throw new IllegalArgumentException("Invalid status value");
        }
        if (concentrationFrom != null && concentrationFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("concentrationFrom cannot be negative");
        }
        if (concentrationTo != null && concentrationTo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("concentrationTo cannot be negative");
        }
        if( priceFrom != null && priceFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("priceFrom cannot be negative");
        }
        if( priceTo != null && priceTo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("priceTo cannot be negative");
        }
        if(concentrationFrom != null && concentrationTo != null && concentrationFrom.compareTo(concentrationTo) > 0) {
            throw new IllegalArgumentException("concentrationFrom cannot be greater than concentrationTo");
        }
        if (priceFrom != null && priceTo != null && priceFrom.compareTo(priceTo) > 0) {
            throw new IllegalArgumentException("priceFrom cannot be greater than priceTo");
        }
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException("categoryId must be positive");
        }
        if (brandId != null && brandId <= 0) {
            throw new IllegalArgumentException("brandId must be positive");
        }
        if (warehouseId != null && warehouseId <= 0) {
            throw new IllegalArgumentException("warehouseId must be positive");
        }
        if (brandId != null) {
            brandQueryRepository.findById(brandId)
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        if (categoryId != null) {
            categoryQueryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        if (warehouseId != null) {
            warehouseQueryRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        }


        // Convert status code to enum (only for seller view)
        ProductStatus productStatus = (status != null && isSellerView)
                ? ProductStatus.fromCode(status) : null;

        Page<Product> productPage;

        if (isSellerView && hasRole("SELLER") && !hasRole("ADMIN")) {
            // Seller view: Only their own products
            String currentUserEmail = securityUtils.getCurrentUserEmail();
            Account currentUser = accountRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            productPage = productQueryRepository.findAllByCreatedByWithFilters(
                    currentUser, productStatus, search, categoryId, brandId,warehouseId,
                    priceFrom, priceTo, concentrationFrom, concentrationTo, pageable);
        } else if (isSellerView && hasRole("ADMIN")) {
            // Admin view: All products
            productPage = productQueryRepository.findAllWithFilters(
                    productStatus, search, categoryId, brandId,warehouseId,
                    priceFrom, priceTo, concentrationFrom, concentrationTo, pageable);
        } else {
            // Customer/Guest view: Only ACTIVE products
            productPage = productQueryRepository.findAllActiveProductsWithFilters(
                    search, categoryId, brandId,warehouseId, priceFrom, priceTo,
                    concentrationFrom, concentrationTo, pageable);
        }

        if (isSellerView && !productPage.isEmpty()) {
            List<Long> productIds = productPage.getContent().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            // Batch fetch promotions for all products
            productQueryRepository.fetchPromotionsForProducts(productIds);
        }

        // Map to appropriate response based on view
        List<Object> products = productPage.getContent().stream()
                .map(product -> isSellerView
                        ? productMapper.toSellerResponse(product)
                        : productMapper.toCustomerResponse(product))
                .collect(Collectors.toList());

        // Build pagination info
        ProductListResponse.PaginationInfo pagination = ProductListResponse.PaginationInfo.builder()
                .currentPage(page)
                .totalPages(productPage.getTotalPages())
                .totalItems(productPage.getTotalElements())
                .perPage(limit)
                .build();

        // Build summary (only for seller view)
        ProductListResponse.ProductSummary summary = null;
        if (isSellerView) {
            summary = buildProductSummary();
        }

        return ProductListResponse.builder()
                .products(products)
                .pagination(pagination)
                .summary(summary)
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public Object getProductById(Long productId) {
        log.info("🔍 Fetching product with id: {}", productId);

        Product product = productQueryRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean isSellerView = isAuthenticated() && (hasRole("SELLER") || hasRole("ADMIN"));
        boolean isOwner = isOwner(product);

        // Customer/Guest view: Only ACTIVE products
        if (!isSellerView && product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product not found");
        }

        // Seller view: Only owner can see
        if (isSellerView && hasRole("SELLER") && !hasRole("ADMIN") && !isOwner) {
            throw new ResourceNotFoundException("Product not found");
        }
        if (isSellerView) {
            productQueryRepository.fetchPromotionsForProduct(productId);
        }

        // Return appropriate response
        return isSellerView && (isOwner || hasRole("ADMIN"))
                ? productMapper.toSellerDetailResponse(product)
                : productMapper.toCustomerDetailResponse(product);
    }

    /**
     * Get related products (similar products)
     */
    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public Object getRelatedProducts(Long productId) {
        Product product = productQueryRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Find products in same category, excluding current product
        List<Product> relatedProducts = productQueryRepository
                .findTop10ByCategoryAndStatusAndIdNotAndDeletedAtIsNull(
                        product.getCategory(),
                        ProductStatus.ACTIVE,
                        productId);

        List<ProductCustomerResponse> products = relatedProducts.stream()
                .map(productMapper::toCustomerResponse)
                .collect(Collectors.toList());

        return java.util.Map.of("products", products);
    }

    /**
     * Get product entity by ID with inventories eagerly loaded
     */
    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public Optional<Product> getProductEntityByIdWithInventories(Long productId) {
        log.info("🔍 Fetching product entity with id: {} and inventories", productId);
        return productQueryRepository.findByIdWithInventories(productId);
    }

    // ============= Helper Methods =============

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private boolean isOwner(Product product) {
        if (product.getCreatedBy() == null) {
            return false;
        }
        try {
            String currentUserEmail = securityUtils.getCurrentUserEmail();
            return securityUtils.isOwner(product.getCreatedBy().getEmail());
        } catch (Exception e) {
            return false;
        }
    }

    private ProductListResponse.ProductSummary buildProductSummary() {
        boolean isSeller = hasRole("SELLER") && !hasRole("ADMIN");

        Long total, pending, active, banned;

        if (isSeller) {
            String currentUserEmail = securityUtils.getCurrentUserEmail();
            Account currentUser = accountRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            total = productQueryRepository.countByCreatedBy(currentUser);
            pending = productQueryRepository.countByCreatedByAndStatus(currentUser, ProductStatus.PENDING);
            active = productQueryRepository.countByCreatedByAndStatus(currentUser, ProductStatus.ACTIVE);
            banned = productQueryRepository.countByCreatedByAndStatus(currentUser, ProductStatus.REJECT);
        } else {
            total = productQueryRepository.countAllActive();
            pending = productQueryRepository.countByStatus(ProductStatus.PENDING);
            active = productQueryRepository.countByStatus(ProductStatus.ACTIVE);
            banned = productQueryRepository.countByStatus(ProductStatus.REJECT);
        }

        return ProductListResponse.ProductSummary.builder()
                .total(total)
                .pending(pending)
                .active(active)
                .banned(banned)
                .build();
    }
}