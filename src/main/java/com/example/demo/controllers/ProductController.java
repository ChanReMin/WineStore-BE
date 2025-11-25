package com.example.demo.controllers;

import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.product.*;
import com.example.demo.services.commands.ProductCommandService;
import com.example.demo.services.queries.ProductQueryService;
import com.example.demo.services.cloudinary.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
        private final ProductCommandService productCommandService;
        private final ProductQueryService productQueryService;
        private final CloudinaryService cloudinaryService;

        @GetMapping()
        public ResponseEntity<SuccessResponse<ProductListResponse>> getAllProducts(
                @RequestParam(defaultValue = "1") Integer page,
                @RequestParam(defaultValue = "20") Integer limit,
                @RequestParam(required = false) String search,
                @RequestParam(name = "category_id", required = false) Long categoryId,
                @RequestParam(name = "brand_id", required = false) Long brandId,
                @RequestParam(name = "min_price", required = false) java.math.BigDecimal minPrice,
                @RequestParam(name = "max_price", required = false) java.math.BigDecimal maxPrice,
                @RequestParam(name = "in_stock", required = false) Boolean inStock,
                @RequestParam(required = false) Integer status,
                @RequestParam(name = "sort_by", required = false) String sortBy,
                @RequestParam(name = "sort_order", defaultValue = "desc") String sortOrder,
                @RequestParam(required = false) String view) {

        ProductListResponse data = productQueryService.getAllProducts(
                page, limit, search, categoryId, brandId, minPrice, maxPrice,
                inStock, status, sortBy, sortOrder, view);

                return ResponseEntity.ok(SuccessResponse.<ProductListResponse>builder()
                        .success(true)
                        .data(data)
                        .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<SuccessResponse<Object>> getProductById(@PathVariable Long id) {
                Object data = productQueryService.getProductById(id);

                return ResponseEntity.ok(SuccessResponse.builder()
                        .success(true)
                        .data(data)
                        .build());
        }
        @GetMapping("/{id}/related")
        public ResponseEntity<SuccessResponse<Object>> getRelatedProducts(@PathVariable Long id) {
                Object data = productQueryService.getRelatedProducts(id);

                return ResponseEntity.ok(SuccessResponse.builder()
                        .success(true)
                        .data(data)
                        .build());
        }

        @PostMapping
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<WriteProductResponse>> createProduct(
                @Valid @RequestBody WriteProductRequest request) {

                WriteProductResponse product = productCommandService.createProduct(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(SuccessResponse.<WriteProductResponse>builder()
                        .success(true)
                        .message("Tạo sản phẩm thành công. Đang chờ admin duyệt")
                        .data(product)
                        .build());
        }

        /**
        * 5. PUT /api/v1/products/{product_id} - Cập nhật sản phẩm (Seller Only)
        */
        @PutMapping("/{productId}")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<WriteProductResponse>> updateProduct(
                @PathVariable Long productId,
                @Valid @RequestBody WriteProductRequest request) {

        WriteProductResponse product = productCommandService.updateProduct(productId, request);

                return ResponseEntity.ok(SuccessResponse.<WriteProductResponse>builder()
                        .success(true)
                        .message("Cập nhật sản phẩm thành công")
                        .data(product)
                        .build());
        }

        @DeleteMapping("/{productId}")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<Void>> deleteProduct(@PathVariable Long productId) {
                productCommandService.deleteProduct(productId);

                return ResponseEntity.ok(SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Xóa sản phẩm thành công")
                        .build());
        }

        @PostMapping("/images")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<UploadImageResponse>> uploadProductImage(
                @RequestParam("image") MultipartFile file,
                @RequestParam(value = "product_id", required = false) Long productId) {

                try {
                        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "src/main/resources/cloudinary");

                        UploadImageResponse response = UploadImageResponse.builder()
                                .id(productId)
                                .url((String) uploadResult.get("secure_url"))
                                .build();

                        return ResponseEntity.ok(SuccessResponse.<UploadImageResponse>builder()
                                .success(true)
                                .message("Upload ảnh thành công")
                                .data(response)
                                .build());
                } catch (IOException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(SuccessResponse.<UploadImageResponse>builder()
                                .success(false)
                                .message("Upload ảnh thất bại: " + e.getMessage())
                                .build());
                }
        }
}