package com.example.demo.services.commands.ServiceCommandImpl;
import com.example.demo.dtos.commands.product.AddPromotionsRequest;
import com.example.demo.dtos.responses.product.AddPromotionsResponse;
import com.example.demo.entities.Product;
import com.example.demo.entities.Promotion;
import com.example.demo.entities.PromotionProduct;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.ProductCommandRepository;
import com.example.demo.repositories.commands.PromotionCommandRepository;
import com.example.demo.repositories.commands.PromotionProductCommandRepository;
import com.example.demo.services.commands.ProductPromotionCommandService;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPromotionCommandServiceImpl implements ProductPromotionCommandService {
    private final ProductCommandRepository productCommandRepository;
    private final PromotionCommandRepository promotionCommandRepository;
    private final PromotionProductCommandRepository promotionProductCommandRepository;
    private final SecurityUtils securityUtils;

    @Transactional(transactionManager = "writeTransactionManager")
    public AddPromotionsResponse addPromotionsToProduct(Long productId, AddPromotionsRequest request) {
        log.info("🎁 Adding promotions to product with id: {}", productId);

        // Find product
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null || !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to add promotions to this product");
        }

        // OPTIMIZATION: Fetch all promotions in ONE query instead of N queries
        List<Promotion> promotions = promotionCommandRepository.findAllByIdIn(request.getPromotionIds());

        // Create a map for quick lookup
        Map<Long, Promotion> promotionMap = promotions.stream()
                .collect(Collectors.toMap(Promotion::getId, p -> p));

        // Validate all promotion IDs exist
        List<Long> notFoundIds = request.getPromotionIds().stream()
                .filter(id -> !promotionMap.containsKey(id))
                .collect(Collectors.toList());

        if (!notFoundIds.isEmpty()) {
            throw new ResourceNotFoundException("Promotions not found with ids: " + notFoundIds);
        }

        // OPTIMIZATION: Check existing relationships in ONE query
        List<PromotionProduct> existingRelations = promotionProductCommandRepository
                .findByPromotionIdIn(request.getPromotionIds())
                .stream()
                .filter(pp -> pp.getProduct().getId().equals(productId))
                .collect(Collectors.toList());

        Map<Long, Boolean> existingPromotionIds = existingRelations.stream()
                .collect(Collectors.toMap(pp -> pp.getPromotion().getId(), pp -> true));

        List<AddPromotionsResponse.PromotionInfo> addedPromotions = new ArrayList<>();
        List<PromotionProduct> promotionProductsToSave = new ArrayList<>();

        // Process all promotions
        for (Long promotionId : request.getPromotionIds()) {
            Promotion promotion = promotionMap.get(promotionId);

            // Check if promotion is active
            if (!promotion.isActive()) {
                log.warn("⚠️ Promotion {} ('{}') is not active, skipping", promotionId, promotion.getName());
                throw new IllegalArgumentException("Promotion '" + promotion.getName() + "' is not active");
            }

            // Skip if already assigned
            if (existingPromotionIds.containsKey(promotionId)) {
                log.warn("⚠️ Promotion {} already assigned to product {}, skipping", promotionId, productId);
                continue;
            }

            // Create promotion-product relationship
            PromotionProduct promotionProduct = PromotionProduct.builder()
                    .promotion(promotion)
                    .product(product)
                    .build();

            promotionProductsToSave.add(promotionProduct);

            addedPromotions.add(AddPromotionsResponse.PromotionInfo.builder()
                    .id(promotion.getId())
                    .code(promotion.getCode())
                    .name(promotion.getName())
                    .build());

            log.info("✅ Added promotion {} ('{}') to product {}",
                    promotionId, promotion.getName(), productId);
        }

        // OPTIMIZATION: Batch save all relationships in ONE query
        if (!promotionProductsToSave.isEmpty()) {
            promotionProductCommandRepository.saveAll(promotionProductsToSave);
        }

        log.info("✅ Successfully added {} promotions to product {}", addedPromotions.size(), productId);

        return AddPromotionsResponse.builder()
                .productId(productId)
                .addedPromotions(addedPromotions)
                .build();
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public void removePromotionFromProduct(Long productId, Long promotionId) {
        log.info("🗑️ Removing promotion {} from product {}", promotionId, productId);

        // Find product
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null || !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to remove promotions from this product");
        }

        // Find promotion (just to validate it exists)
        if (!promotionCommandRepository.existsById(promotionId)) {
            throw new ResourceNotFoundException("Promotion not found with id: " + promotionId);
        }

        // OPTIMIZATION: Find with JOIN FETCH to avoid additional queries
        PromotionProduct promotionProduct = promotionProductCommandRepository
                .findByPromotionIdAndProductId(promotionId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Promotion is not assigned to this product"));

        // Delete relationship
        promotionProductCommandRepository.delete(promotionProduct);

        log.info("✅ Successfully removed promotion {} from product {}", promotionId, productId);
    }
}
