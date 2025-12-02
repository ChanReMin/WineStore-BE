package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductStatusRequest;
import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.UpdateProductStatusResponse;
import com.example.demo.dtos.responses.product.WriteProductResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Brand;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.BrandCommandRepository;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.CategoryCommandRepository;
import com.example.demo.repositories.commands.ProductCommandRepository;
import com.example.demo.services.commands.ProductCommandService;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.services.AiService;
import com.example.demo.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductCommandRepository productCommandRepository;
    private final CategoryCommandRepository categoryCommandRepository;
    private final BrandCommandRepository brandCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final AiService aiService;
    private final CloudinaryService cloudinaryService;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;

    @Transactional(transactionManager = "writeTransactionManager")
    public WriteProductResponse createProduct(CreateProductRequest request) {
        log.info("🆕 Creating product with name: {}", request.getName());

        // Get current user
        Long Iduser = SecurityUtils.getCurrentUserUuid();
        Account currentUser = accountCommandRepository.findById(Iduser)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check duplicate product name
        if (productCommandRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Validate category exists
        Category category = categoryCommandRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Validate brand exists
        Brand brand = brandCommandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        if (productCommandRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Validate image file first (quick validation)
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new IllegalArgumentException("Product image is required");
        }

        // Map request to entity
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);

        // Set placeholder image URL (will be updated asynchronously)
        product.setImages(cloudinaryService.getPlaceholderUrl());
        // Set creator
        product.setCreatedBy(currentUser);

        // Set default status as PENDING (waiting for admin approval)
        product.setStatus(ProductStatus.PENDING);

        // Save product to Write DB FIRST (fast response)
        Product savedProduct = productCommandRepository.save(product);
        log.info("✅ Product created successfully with id: {} by user: {} and status: PENDING",
                savedProduct.getId(), Iduser);
//        aiService.sendProductToAI(savedProduct.getId());

        // Upload image asynchronously (non-blocking)
        cloudinaryService.uploadImageAsync(savedProduct.getId(), request.getImage())
                .thenAccept(imageUrl -> log.info("✅ Async image upload completed for product {}: {}",
                        savedProduct.getId(), imageUrl))
                .exceptionally(ex -> {
                    log.error("❌ Async image upload failed for product {}", savedProduct.getId(), ex);
                    return null;
                });

        return productMapper.toCreateResponse(savedProduct);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public WriteProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("✏️ Updating product with id: {}", id);

        Long currentUser = SecurityUtils.getCurrentUserUuid();
        log.info("Current user ID: {}", currentUser);
        // Find product with details to avoid N+1
        Product product = productCommandRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getCreatedBy() == null ||
                !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to update this product");
        }

        // Only allow update for PENDING or ACTIVE status
        if (product.getStatus() != ProductStatus.PENDING && product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Cannot update product with status: " + product.getStatus().getDescription());
        }

        // Check duplicate name (excluding current product)
        if (productCommandRepository.existsByNameAndIdNotAndDeletedAtIsNull(request.getName(), id)) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Validate category exists if changed
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryCommandRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Validate brand exists if changed
        if (!product.getBrand().getId().equals(request.getBrandId())) {
            Brand brand = brandCommandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }
        // Check duplicate name (excluding current product)
        if (productCommandRepository.existsByNameAndIdNotAndDeletedAtIsNull(request.getName(), id)) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Update product fields
        productMapper.updateEntity(product, request);

        product.setStatus(ProductStatus.PENDING);
        product.setApprovedAt(null);
        product.setApprovedBy(null);

        String oldImageUrl = product.getImages();
        boolean uploadingNewFile = request.getImage() != null && !request.getImage().isEmpty();
        boolean userDeleteImage = request.getImages() != null && request.getImages().trim().isEmpty();


        // Case 1: User WANTS TO DELETE PHOTO
        if (userDeleteImage) {
            log.info("🗑 User wants to delete image");

            if (!uploadingNewFile) {
                throw new IllegalArgumentException("You must upload new image if deleting the old one");
            }

            product.setImages(null);

            if (oldImageUrl != null && !oldImageUrl.contains("placeholder")) {
                cloudinaryService.deleteImage(oldImageUrl);
            }
        }

        // Case 2: User UPLOAD NEW PHOTO
        else if (uploadingNewFile) {
            log.info("📸 User uploading new product image");

            // Set placeholder immediately
            product.setImages(cloudinaryService.getPlaceholderUrl());
            Product tempSaved = productCommandRepository.save(product);

            // Upload new image asynchronously
            cloudinaryService.uploadImageAsync(tempSaved.getId(), request.getImage())
                    .thenAccept(newImageUrl -> {
                        log.info("✅ Async image update completed for product {}: {}",
                                tempSaved.getId(), newImageUrl);

                        // Delete old image after successful upload
                        if (oldImageUrl != null && !oldImageUrl.contains("placeholder")) {
                            cloudinaryService.deleteImage(oldImageUrl);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("❌ Async image update failed for product {}", tempSaved.getId(), ex);

                        // Restore old image on failure
                        try {
                            Product p = productCommandRepository.findById(tempSaved.getId()).orElse(null);
                            if (p != null) {
                                p.setImages(oldImageUrl != null ? oldImageUrl :
                                        "https://via.placeholder.com/800x800?text=Upload+Failed");
                                productCommandRepository.save(p);
                            }
                        } catch (Exception e) {
                            log.error("Failed to restore old image", e);
                        }
                        return null;
                    });
        }
        // Case 3: NO CHANGE TO THE PHOTO (keep the old photo)
        else {
            log.info("ℹ️ Keep existing image");
            if (oldImageUrl == null) {
                throw new IllegalStateException("Product must have an image");
            }
        }

        Product updatedProduct = productCommandRepository.save(product);
        log.info("Product updated successfully with id: {} by user: {}, status reset to PENDING",
                updatedProduct.getId(), currentUser);
//        aiService.sendProductToAI(updatedProduct.getId());
        return productMapper.toCreateResponse(updatedProduct);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteProduct(Long id) {
        log.info("🗑️  Deleting product with id: {}", id);

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();

        Product product = productCommandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null ||
                !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to delete this product");
        }

        product.softDelete();
        productCommandRepository.save(product);

        log.info("✅ Product soft deleted successfully with id: {} by user: {}", id, currentUserEmail);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateProductStatusResponse updateProductStatus(Long productId, UpdateProductStatusRequest request) {
        log.info("🔄 Updating product status - productId: {}, newStatus: {}", productId, request.getStatus());

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();

        // Find product
        Product product = productCommandRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        boolean isSeller = securityUtils.hasRole("SELLER");
        // Check if current user is the creator of this product
        if (isSeller) {
            throw new ForbiddenException("You don't have permission to update this product status");
        }

        // Validate status value (0-2 only, no 3/BANNED via seller endpoint)
        if (request. getStatus() < 0 || request.getStatus() > 2) {
            throw new IllegalArgumentException("Invalid status value.  Allowed values: 0 (Pending), 1 (Active), 2 (Inactive)");
        }

        // Update product status
        ProductStatus newStatus = ProductStatus.fromCode(request.getStatus());
        product.setStatus(newStatus);

        // Save updated product
        Product updatedProduct = productCommandRepository.save(product);

        log.info("✅ Product status updated successfully - productId: {}, status: {} by user: {}",
                productId, newStatus.getDescription(), currentUserEmail);

        return UpdateProductStatusResponse.builder()
                .id(updatedProduct.getId())
                .status(updatedProduct.getStatus(). getCode())
                .statusText(updatedProduct.getStatus(). getDescription())
                .build();
    }
}