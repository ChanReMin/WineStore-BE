package com.example.demo.controllers;

import com.example.demo.dtos.commands.product.*;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.product.*;
import com.example.demo.services.commands.ProductCommandService;
import com.example.demo.services.commands.ProductPromotionCommandService;
import com.example.demo.services.queries.ProductQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Slf4j
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products Management")
public class ProductController {
        private final ProductCommandService productCommandService;
        private final ProductQueryService productQueryService;
        private final ProductPromotionCommandService productPromotionCommandService;

        @GetMapping()
        public ResponseEntity<SuccessResponse<ProductListResponse>> getAllProducts(
                @RequestParam(required = false, defaultValue = "1") Integer page,
                @RequestParam(required = false, defaultValue = "10") Integer limit,
                @RequestParam(required = false) String search,
                @RequestParam(required = false) Integer status,
                @RequestParam(required = false) Long categoryId,
                @RequestParam(required = false) Long warehouseId,
                @RequestParam(required = false) Long brandId,
                @RequestParam(required = false) BigDecimal priceFrom,
                @RequestParam(required = false) BigDecimal priceTo,
                @RequestParam(required = false) BigDecimal concentrationFrom,
                @RequestParam(required = false) BigDecimal concentrationTo) {

                ProductListResponse response = productQueryService.getAllProducts(
                        page, limit, search, status, categoryId, brandId,warehouseId,
                        priceFrom, priceTo, concentrationFrom, concentrationTo);

                return ResponseEntity.ok(SuccessResponse.<ProductListResponse>builder()
                        .success(true)
                        .data(response)
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

        @PreAuthorize("hasRole('SELLER')")
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<SuccessResponse<WriteProductResponse>> createProduct(
                @Valid @ModelAttribute CreateProductRequest request) {

                log.info("📝 Creating product: {}", request.getName());

                WriteProductResponse product = productCommandService.createProduct(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(SuccessResponse.<WriteProductResponse>builder()
                        .success(true)
                        .message("Product created successfully. Awaiting admin approval")
                        .data(product)
                        .build());
        }


        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<WriteProductResponse>> updateProduct(
                @PathVariable Long id,
                @Valid @ModelAttribute UpdateProductRequest request) {

        WriteProductResponse product = productCommandService.updateProduct(id, request);

                return ResponseEntity.ok(SuccessResponse.<WriteProductResponse>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(product)
                        .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<Void>> deleteProduct(@PathVariable Long id) {
                productCommandService.deleteProduct(id);

                return ResponseEntity.ok(SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .build());
        }

        @PutMapping("/{id}/status")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SuccessResponse<UpdateProductStatusResponse>> updateProductStatus(
                @PathVariable Long id,
                @Valid @RequestBody UpdateProductStatusRequest request) {

                UpdateProductStatusResponse response = productCommandService.updateProductStatus(id, request);

                return ResponseEntity.ok(SuccessResponse.<UpdateProductStatusResponse>builder()
                        .success(true)
                        .message("Update product status successfully")
                        .data(response)
                        .build());
        }

        @PostMapping("/{id}/promotions")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<AddPromotionsResponse>> addPromotionsToProduct(
                @PathVariable Long id,
                @Valid @RequestBody AddPromotionsRequest request) {

                AddPromotionsResponse response = productPromotionCommandService.addPromotionsToProduct(id, request);

                return ResponseEntity.ok(SuccessResponse.<AddPromotionsResponse>builder()
                        .success(true)
                        .message("Promotions added to product successfully")
                        .data(response)
                        .build());
        }

        @DeleteMapping("/{productId}/promotions/{promotionId}")
        @PreAuthorize("hasRole('SELLER')")
        public ResponseEntity<SuccessResponse<Void>> removePromotionFromProduct(
                @PathVariable Long productId,
                @PathVariable Long promotionId) {

                productPromotionCommandService.removePromotionFromProduct(productId, promotionId);

                return ResponseEntity.ok(SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Promotion removed from product successfully")
                        .build());
        }
}