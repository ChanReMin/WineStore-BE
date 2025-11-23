package com.example.demo.controllers;

import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.product.CreateProductResponse;
import com.example.demo.dtos.responses.product.ProductResponse;
import com.example.demo.services.commands.ProductCommandService;
import com.example.demo.services.queries.ProductQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    /**
     * 1. Lấy danh sách sản phẩm
     * GET /api/v1/products
     * Quyền: SELLER
     */
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search) {

        ProductResponse data = productQueryService.getAllProducts(page, limit, status, search);

        SuccessResponse<ProductResponse> response = SuccessResponse.<ProductResponse>builder()
                .success(true)
                .data(data)
                .message("Product list retrieved successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 2. Tạo sản phẩm mới
     * POST /api/v1/products
     * Quyền: SELLER
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<CreateProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        CreateProductResponse product = productCommandService.createProduct(request);

        SuccessResponse<CreateProductResponse> response = SuccessResponse.<CreateProductResponse>builder()
                .success(true)
                .message("Product created successfully and is pending admin approval.")
                .data(product)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3. Cập nhật sản phẩm
     * PUT /api/v1/products/{product_id}
     * Quyền: SELLER
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<CreateProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {

        CreateProductResponse product = productCommandService.updateProduct(productId, request);

        SuccessResponse<CreateProductResponse> response = SuccessResponse.<CreateProductResponse>builder()
                .success(true)
                .message("Product updated successfully and is pending admin re-approval.")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 4. Xóa sản phẩm
     * DELETE /api/v1/products/{product_id}
     * Quyền: SELLER
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productCommandService.deleteProduct(productId);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .success(true)
                .message("Product deleted successfully.")
                .build();

        return ResponseEntity.ok(response);
    }
}