package com.example.demo.services.commands;

import com.example.demo.commons.enums.DiscountType;
import com.example.demo.configs.SecurityUtils;
import com.example.demo.dtos.commands.promotion.CreatePromotionRequest;
import com.example.demo.dtos.commands.promotion.ExportPromotionStatisticsRequest;
import com.example.demo.dtos.commands.promotion.TogglePromotionStatusRequest; // Import this
import com.example.demo.dtos.commands.promotion.UpdatePromotionRequest;
import com.example.demo.dtos.responses.promotion.CreatePromotionResponse;
import com.example.demo.dtos.responses.promotion.ExportPromotionStatisticsResponse;
import com.example.demo.dtos.responses.promotion.TogglePromotionStatusResponse; // Import this
import com.example.demo.dtos.responses.promotion.UpdatePromotionResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Product;
import com.example.demo.entities.Promotion;
import com.example.demo.entities.PromotionProduct;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.PromotionCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.CategoryQueryRepository;
import com.example.demo.repositories.queries.ProductQueryRepository;
import com.example.demo.repositories.queries.PromotionQueryRepository;
import com.example.demo.dtos.mappers.promotion.PromotionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.commons.enums.DiscountType.PERCENTAGE;

@Service
@RequiredArgsConstructor
public class PromotionCommandService {

    private final PromotionCommandRepository promotionCommandRepository;
    private final PromotionQueryRepository promotionQueryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final CategoryQueryRepository categoryQueryRepository;
    private final SecurityUtils securityUtils;
    private final PromotionMapper promotionMapper;
    private final AccountQueryRepository accountQueryRepository;


    @Transactional
    public CreatePromotionResponse createPromotion(CreatePromotionRequest request) {
        // 1. Validate Code Uniqueness
        if (promotionCommandRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("code", "Promotion code already exists");
        }

        // 2. Validate Discount Value
        if (request.getDiscount_type() == PERCENTAGE &&
                (request.getDiscount_value().compareTo(BigDecimal.ZERO) <= 0 || request.getDiscount_value().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Percentage discount value must be between 0 and 100");
        }
        if (request.getDiscount_type() == DiscountType.FIXED_AMOUNT &&
                request.getDiscount_value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fixed amount discount value must be greater than 0");
        }

        // 3. Validate Dates
        if (request.getStart_date().isAfter(request.getEnd_date())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // 4. Get Current Seller Account by Email
        String currentSellerEmail = securityUtils.getCurrentUserEmail();
        if (currentSellerEmail == null) {
            throw new ForbiddenException("User is not authenticated or does not have permission to perform this action.");
        }
        Account createdByAccount = accountQueryRepository.findByEmail(currentSellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email", currentSellerEmail));
        Long currentSellerAccountId = createdByAccount.getId();


        // 5. Determine Applicable Product IDs
        Set<Long> applicableProductIds = new HashSet<>();

        // Add product_ids from request
        if (request.getProduct_ids() != null && !request.getProduct_ids().isEmpty()) {
            applicableProductIds.addAll(request.getProduct_ids());
        }

        // Add product_ids from categories
        if (request.getCategory_ids() != null && !request.getCategory_ids().isEmpty()) {
            List<Product> productsInCategories = productQueryRepository.findByCategoryIdIn(request.getCategory_ids());
            productsInCategories.forEach(p -> applicableProductIds.add(p.getId()));
        }

        // Filter out excluded_product_ids
        if (request.getExcluded_product_ids() != null && !request.getExcluded_product_ids().isEmpty()) {
            applicableProductIds.removeAll(request.getExcluded_product_ids());
        }

        // 6. Fetch and Validate Applicable Products
        List<Product> applicableProducts = productQueryRepository.findAllById(applicableProductIds);

        if (applicableProducts.size() != applicableProductIds.size()) {
            // Some product IDs were not found
            Set<Long> foundProductIds = applicableProducts.stream().map(Product::getId).collect(Collectors.toSet());
            applicableProductIds.removeAll(foundProductIds);
            throw new ResourceNotFoundException("Product", "id", applicableProductIds.toString());
        }

        // Ensure products are owned by the current seller
        List<Product> sellerOwnedProducts = applicableProducts.stream()
                .filter(p -> p.getCreatedBy() != null && p.getCreatedBy().getId().equals(currentSellerAccountId))
                .collect(Collectors.toList());

        if (sellerOwnedProducts.size() != applicableProducts.size()) {
            // Some products are not owned by the current seller
            Set<Long> notOwnedProductIds = new HashSet<>(applicableProductIds);
            sellerOwnedProducts.forEach(p -> notOwnedProductIds.remove(p.getId()));
            throw new ForbiddenException("You do not have permission to apply promotion to products with ID: " + notOwnedProductIds);
        }

        // 7. Build Promotion Entity
        Promotion promotion = Promotion.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscount_type())
                .discountValue(request.getDiscount_value())
                .startDate(request.getStart_date())
                .endDate(request.getEnd_date())
                .maxUsage(request.getMax_usage())
                .status(request.getStatus() != null ? request.getStatus() : 1) // Default to Active
                .createdBy(createdByAccount)
                .build();

        // 8. Build PromotionProduct entities
        List<PromotionProduct> promotionProducts = applicableProducts.stream()
                .map(product -> PromotionProduct.builder()
                        .promotion(promotion)
                        .product(product)
                        .build())
                .collect(Collectors.toList());
        promotion.setPromotionProducts(promotionProducts);

        // 9. Save Promotion
        Promotion savedPromotion = promotionCommandRepository.save(promotion);

        // Note: The 'conditions' object from the request is not directly mapped to the Promotion entity
        // as there are no corresponding fields. If needed, the Promotion entity schema must be extended
        // to support these conditions (e.g., as a JSONB column or a separate entity).

        return promotionMapper.toCreatePromotionResponse(savedPromotion);
    }

    @Transactional
    public UpdatePromotionResponse updatePromotion(Long promotionId, UpdatePromotionRequest request) {
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
            throw new ForbiddenException("You do not have permission to update this promotion.");
        }

        // 4. Check Status for Editing (Cannot edit ended promotion)
        if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot edit ended promotion.");
        }

        // 5. Apply Partial Updates
        if (request.getCode() != null && !request.getCode().equals(promotion.getCode())) {
            if (promotionCommandRepository.existsByCode(request.getCode())) {
                throw new DuplicateResourceException("code", "Promotion code already exists");
            }
            promotion.setCode(request.getCode());
        }
        if (request.getName() != null) {
            promotion.setName(request.getName());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getDiscount_type() != null) {
            promotion.setDiscountType(request.getDiscount_type());
        }
        if (request.getDiscount_value() != null) {
            // Re-validate discount value if type or value is updated
            DiscountType effectiveDiscountType = request.getDiscount_type() != null ? request.getDiscount_type() : promotion.getDiscountType();
            BigDecimal effectiveDiscountValue = request.getDiscount_value();

            if (effectiveDiscountType == PERCENTAGE &&
                    (effectiveDiscountValue.compareTo(BigDecimal.ZERO) <= 0 || effectiveDiscountValue.compareTo(BigDecimal.valueOf(100)) > 0)) {
                throw new IllegalArgumentException("Percentage discount value must be between 0 and 100");
            }
            if (effectiveDiscountType == DiscountType.FIXED_AMOUNT &&
                    effectiveDiscountValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Fixed amount discount value must be greater than 0");
            }
            promotion.setDiscountValue(effectiveDiscountValue);
        }
        if (request.getStart_date() != null) {
            promotion.setStartDate(request.getStart_date());
        }
        if (request.getEnd_date() != null) {
            promotion.setEndDate(request.getEnd_date());
        }
        if (request.getStart_date() != null || request.getEnd_date() != null) {
            LocalDateTime effectiveStartDate = request.getStart_date() != null ? request.getStart_date() : promotion.getStartDate();
            LocalDateTime effectiveEndDate = request.getEnd_date() != null ? request.getEnd_date() : promotion.getEndDate();
            if (effectiveStartDate.isAfter(effectiveEndDate)) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }
        if (request.getMax_usage() != null) {
            promotion.setMaxUsage(request.getMax_usage());
        }
        if (request.getStatus() != null) {
            promotion.setStatus(request.getStatus());
        }

        // 6. Recalculate PromotionProduct relationships if product/category IDs are provided
        if ((request.getProduct_ids() != null && !request.getProduct_ids().isEmpty()) ||
                (request.getCategory_ids() != null && !request.getCategory_ids().isEmpty()) ||
                (request.getExcluded_product_ids() != null && !request.getExcluded_product_ids().isEmpty())) {

            Set<Long> updatedApplicableProductIds = new HashSet<>();

            // Add product_ids from request
            if (request.getProduct_ids() != null && !request.getProduct_ids().isEmpty()) {
                updatedApplicableProductIds.addAll(request.getProduct_ids());
            }

            // Add product_ids from categories
            if (request.getCategory_ids() != null && !request.getCategory_ids().isEmpty()) {
                List<Product> productsInCategories = productQueryRepository.findByCategoryIdIn(request.getCategory_ids());
                productsInCategories.forEach(p -> updatedApplicableProductIds.add(p.getId()));
            }

            // Filter out excluded_product_ids
            if (request.getExcluded_product_ids() != null && !request.getExcluded_product_ids().isEmpty()) {
                updatedApplicableProductIds.removeAll(request.getExcluded_product_ids());
            }

            List<Product> updatedApplicableProducts = productQueryRepository.findAllById(updatedApplicableProductIds);

            if (updatedApplicableProducts.size() != updatedApplicableProductIds.size()) {
                Set<Long> foundProductIds = updatedApplicableProducts.stream().map(Product::getId).collect(Collectors.toSet());
                updatedApplicableProductIds.removeAll(foundProductIds);
                throw new ResourceNotFoundException("Product", "id", updatedApplicableProductIds.toString());
            }

            // Ensure products are owned by the current seller
            List<Product> sellerOwnedProducts = updatedApplicableProducts.stream()
                    .filter(p -> p.getCreatedBy() != null && p.getCreatedBy().getId().equals(currentSellerAccountId))
                    .collect(Collectors.toList());

            if (sellerOwnedProducts.size() != updatedApplicableProducts.size()) {
                Set<Long> notOwnedProductIds = new HashSet<>(updatedApplicableProductIds);
                sellerOwnedProducts.forEach(p -> notOwnedProductIds.remove(p.getId()));
                throw new ForbiddenException("You do not have permission to apply promotion to products with ID: " + notOwnedProductIds);
            }

            // Clear old promotion products and add new ones
            promotion.getPromotionProducts().clear(); // Orphan removal should handle deletion
            updatedApplicableProducts.forEach(product -> promotion.addPromotionProduct(PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .build()));

        }

        // 7. Save Promotion
        Promotion savedPromotion = promotionCommandRepository.save(promotion);

        // Note: 'conditions' object not directly mapped.

        return promotionMapper.toUpdatePromotionResponse(savedPromotion);
    }

    @Transactional
    public void deletePromotion(Long promotionId) {
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
            throw new ForbiddenException("You do not have permission to delete this promotion.");
        }

        // 4. Check Status for Deletion (Cannot delete active and used promotion)
        if (promotion.getStatus() == 1 && promotion.getUsedCount() != null && promotion.getUsedCount() > 0) {
            throw new IllegalArgumentException("Cannot delete active and used promotion.");
        }

        // 5. Delete Promotion
        promotionCommandRepository.delete(promotion);
    }

        @Transactional
        public TogglePromotionStatusResponse togglePromotionStatus(Long promotionId, TogglePromotionStatusRequest request) {
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
                throw new ForbiddenException("You do not have permission to change the status of this promotion.");
            }
    
            // 4. Update Status
            promotion.setStatus(request.getStatus());
    
            // 5. Save Promotion
            Promotion savedPromotion = promotionCommandRepository.save(promotion);
    
            return promotionMapper.toTogglePromotionStatusResponse(savedPromotion);
        }
    
        @Transactional
        public ExportPromotionStatisticsResponse exportPromotionStatistics(Long promotionId, ExportPromotionStatisticsRequest request) {
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
                throw new ForbiddenException("You do not have permission to export statistics for this promotion.");
            }
    
            // 4. Generate a unique export_id (simple example, ideally more robust)
            String exportId = "EXP-PROM-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + "-" + promotionId;
    
            // 5. Return processing response
            // In a real application, this would trigger an asynchronous task to generate the file
            // and update the download URL once completed.
            return ExportPromotionStatisticsResponse.builder()
                    .export_id(exportId)
                    .status("processing")
                    .download_url(null)
                    .build();
        }

    }