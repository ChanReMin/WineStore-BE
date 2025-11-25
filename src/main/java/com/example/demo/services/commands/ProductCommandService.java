package com.example.demo.services.commands;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.mappers.product.ProductMapper;
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
import com.example.demo.configs.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCommandService {

    private final ProductCommandRepository productCommandRepository;
    private final CategoryCommandRepository categoryCommandRepository;
    private final BrandCommandRepository brandCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;

    @Transactional(transactionManager = "writeTransactionManager")
    public WriteProductResponse createProduct(WriteProductRequest request) {
        log.info("📝 Creating product with name: {}", request.getName());

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();
        Account currentUser = accountCommandRepository.findByEmail(currentUserEmail)
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

        // Map request to entity
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);

        // Set creator
        product.setCreatedBy(currentUser);

        // Set default status as PENDING (waiting for admin approval)
        product.setStatus(ProductStatus.PENDING);

        // Save product to Write DB
        Product savedProduct = productCommandRepository.save(product);
        log.info("✅ Product created successfully with id: {} by user: {} and status: PENDING",
                savedProduct.getId(), currentUserEmail);
        return productMapper.toCreateResponse(savedProduct);
    }

    @Transactional(transactionManager = "writeTransactionManager")
    public WriteProductResponse updateProduct(Long id, WriteProductRequest request) {
        log.info("✏️  Updating product with id: {}", id);

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();

        // Find product with details to avoid N+1
        Product product = productCommandRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null ||
                !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to update this product");
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

        // Update product fields
        productMapper.updateEntity(product, request);

        // Reset approval status when product is updated
        product.setStatus(ProductStatus.PENDING);
        product.setApprovedAt(null);
        product.setApprovedBy(null);

        // Save updated product to Write DB
        Product updatedProduct = productCommandRepository.save(product);
        log.info("✅ Product updated successfully with id: {} by user: {}, status reset to PENDING",
                updatedProduct.getId(), currentUserEmail);


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
}