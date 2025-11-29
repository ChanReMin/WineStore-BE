package com.example.demo.services.queries.serviceQueryImpl;


import com.example.demo.dtos.mappers.promotion.PromotionMapper;
import com.example.demo.dtos.responses.promotion.PromotionDetailResponse;
import com.example.demo.dtos.responses.promotion.PromotionListResponse;
import com.example.demo.dtos.responses.promotion.PromotionResponse;
import com.example.demo.dtos.responses.promotion.PromotionStatisticsResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Product;
import com.example.demo.entities.Promotion;
import com.example.demo.entities.PromotionProduct;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.AccountQueryRepository; // Import AccountQueryRepository
import com.example.demo.repositories.queries.PromotionQueryRepository;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionQueryServiceImpl {

    private final PromotionQueryRepository promotionQueryRepository;
    private final PromotionMapper promotionMapper;
    private final SecurityUtils securityUtils; // Inject SecurityUtils
    private final AccountQueryRepository accountQueryRepository; // Inject AccountQueryRepository


    public PromotionListResponse getPromotions(int page, int limit, Integer status, String search, String sortBy, String sortOrder) {
        String sortField = sortBy;
        if (sortBy.equals("created_at")) {
            sortField = "createdAt";
        } else if (sortBy.equals("start_date")) {
            sortField = "startDate";
        } else if (sortBy.equals("end_date")) {
            sortField = "endDate";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortField);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Specification<Promotion> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (search != null && !search.isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchLike);
                Predicate codeLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchLike);
                predicates.add(criteriaBuilder.or(nameLike, codeLike));
            }

            // Filter promotions by products created by the authenticated seller
            String currentSellerEmail = securityUtils.getCurrentUserEmail();
            if (currentSellerEmail != null) {
                Account currentSellerAccount = accountQueryRepository.findByEmail(currentSellerEmail)
                        .orElse(null); // If account not found for email (shouldn't happen if authenticated), return no promotions
                if (currentSellerAccount != null) {
                    Long currentSellerAccountId = currentSellerAccount.getId();
                    // Join from Promotion to PromotionProduct
                    Join<Promotion, PromotionProduct> promotionProductJoin = root.join("promotionProducts", JoinType.INNER);
                    // Join from PromotionProduct to Product
                    Join<PromotionProduct, Product> productJoin = promotionProductJoin.join("product", JoinType.INNER);
                    // Join from Product to Account (createdBy)
                    Join<Product, Account> productCreatorAccountJoin = productJoin.join("createdBy", JoinType.INNER);

                    predicates.add(criteriaBuilder.equal(productCreatorAccountJoin.get("id"), currentSellerAccountId));
                } else {
                    // If authenticated but account not found, ensure no promotions are returned
                    predicates.add(criteriaBuilder.disjunction()); // Always false predicate
                }
            }
            query.distinct(true); // Ensure distinct promotions if a product is in multiple promotion_products, or one promotion applies to multiple seller products.

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Promotion> promotionPage = promotionQueryRepository.findAll(spec, pageable);

        List<PromotionResponse> promotionResponses = promotionPage.getContent().stream()
                .map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());

        return PromotionListResponse.builder()
                .promotions(promotionResponses)
                .pagination(PromotionListResponse.PaginationInfo.builder()
                        .current_page(promotionPage.getNumber() + 1)
                        .total_pages(promotionPage.getTotalPages())
                        .total_items(promotionPage.getTotalElements())
                        .per_page(promotionPage.getSize())
                        .build())
                .build();
    }

    public PromotionDetailResponse getPromotionDetails(Long promotionId) {
        Promotion promotion = promotionQueryRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        // Assuming promotion.getPromotionProducts() and product details are eagerly fetched via EntityGraph
        // If not, LazyInitializationException might occur here.

        return promotionMapper.toPromotionDetailResponse(promotion);
    }

    public PromotionStatisticsResponse getPromotionStatistics(Long promotionId) {
        // 1. Get Current Seller Account by Email
        String currentSellerEmail = securityUtils.getCurrentUserEmail();
        if (currentSellerEmail == null) {
            throw new ForbiddenException("User is not authenticated or does not have permission to perform this action.");
        }
        Account createdByAccount = accountQueryRepository.findByEmail(currentSellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email", currentSellerEmail));
        Long currentSellerAccountId = createdByAccount.getId();

        // 2. Retrieve existing Promotion
        Promotion promotion = promotionQueryRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", promotionId));

        // 3. Check Ownership
        if (!promotion.getCreatedBy().getId().equals(currentSellerAccountId)) {
            throw new ForbiddenException("You do not have permission to view statistics for this promotion.");
        }

        // 4. Populate PromotionStatisticsResponse (with placeholders for complex stats due to schema limitations)
        Integer totalUsage = promotion.getUsedCount() != null ? promotion.getUsedCount() : 0;
        Integer maxUsage = promotion.getMaxUsage() != null ? promotion.getMaxUsage() : 0;
        double usageRate = (maxUsage > 0) ? ((double) totalUsage / maxUsage) * 100 : 0.0;

        return PromotionStatisticsResponse.builder()
                .promotion_id(promotion.getId())
                .promotion_code(promotion.getCode())
                .promotion_name(promotion.getName())
                .total_usage(totalUsage)
                .max_usage(maxUsage)
                .remaining_usage(maxUsage - totalUsage)
                .usage_rate(usageRate)
                .total_discount_amount(BigDecimal.ZERO) // Placeholder
                .total_orders(0L) // Placeholder
                .total_revenue(BigDecimal.ZERO) // Placeholder
                .average_order_value(BigDecimal.ZERO) // Placeholder
                .unique_customers(0L) // Placeholder
                .new_customers(0L) // Placeholder
                .returning_customers(0L) // Placeholder
                .conversion_rate(0.0) // Placeholder
                .usage_by_date(Collections.emptyList()) // Placeholder
                .top_products(Collections.emptyList()) // Placeholder
                .period(PromotionStatisticsResponse.Period.builder()
                        .start_date(promotion.getStartDate())
                        .end_date(promotion.getEndDate())
                        .build())
                .build();
    }
}