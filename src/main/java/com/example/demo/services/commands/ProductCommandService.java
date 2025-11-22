package com.example.demo.services.commands;

import com.example.demo.commons.annotations.WriteService;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.mappers.product.ProductMapper;
import com.example.demo.dtos.responses.product.CreateProductResponse;
import com.example.demo.entities.Brand;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.BrandRepository;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
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
    private final ProductMapper productMapper;

    @Transactional
    @WriteService
    public CreateProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

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

        // Set default status as PENDING (waiting for admin approval)
        product.setStatus(ProductStatus.PENDING);

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {} and status: PENDING", savedProduct.getId());

        return productMapper.toCreateResponse(savedProduct);
    }

    @Transactional
    @WriteService
    public CreateProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);

        // Find product with details to avoid N+1
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

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
        log.info("Product updated successfully with id: {}, status reset to PENDING", updatedProduct.getId());

        return productMapper.toCreateResponse(updatedProduct);
    }

    @Transactional
    @WriteService
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.softDelete();
        productRepository.save(product);

        log.info("Product soft deleted successfully with id: {}", id);
    }
}