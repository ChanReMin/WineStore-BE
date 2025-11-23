package com.example.demo.services.commands;

import com.example.demo.commons.annotations.WriteService;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.CreateProductResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Brand;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.BrandRepository;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.configs.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AccountRepository accountRepository;
    private final ProductMapper productMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    @WriteService
    public CreateProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();
        Account currentUser = accountRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check duplicate product name
        if (productRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Validate brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));

        // Map request to entity
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);

        // Set creator
        product.setCreatedBy(currentUser);

        // Set default status as PENDING (waiting for admin approval)
        product.setStatus(ProductStatus.PENDING);

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {} by user: {} and status: PENDING",
                savedProduct.getId(), currentUserEmail);

        return productMapper.toCreateResponse(savedProduct);
    }

    @Transactional
    @WriteService
    public CreateProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();

        // Find product with details to avoid N+1
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null ||
                !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to update this product");
        }

        // Check duplicate name (excluding current product)
        if (productRepository.existsByNameAndIdNotAndDeletedAtIsNull(request.getName(), id)) {
            throw new DuplicateResourceException("Product with name '" + request.getName() + "' already exists", "name");
        }

        // Validate category exists if changed
        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Validate brand exists if changed
        if (!product.getBrand().getId().equals(request.getBrandId())) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        // Update product fields
        productMapper.updateEntity(product, request);

        // Reset approval status when product is updated
        product.setStatus(ProductStatus.PENDING);
        product.setApprovedAt(null);
        product.setApprovedBy(null);

        // Save updated product
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with id: {} by user: {}, status reset to PENDING",
                updatedProduct.getId(), currentUserEmail);

        return productMapper.toCreateResponse(updatedProduct);
    }

    @Transactional
    @WriteService
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        // Get current user
        String currentUserEmail = securityUtils.getCurrentUserEmail();

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if current user is the creator of this product
        if (product.getCreatedBy() == null ||
                !securityUtils.isOwner(product.getCreatedBy().getEmail())) {
            throw new ForbiddenException("You don't have permission to delete this product");
        }

        product.softDelete();
        productRepository.save(product);

        log.info("Product soft deleted successfully with id: {} by user: {}", id, currentUserEmail);
    }
}