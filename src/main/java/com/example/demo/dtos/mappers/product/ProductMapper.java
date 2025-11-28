package com.example.demo.dtos.mappers.product;

import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.responses.product.*;
import com.example.demo.entities.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductMapper {

    private final ObjectMapper objectMapper;

    // ============= Customer View Mappings =============
    public ProductCustomerResponse toCustomerResponse(Product product) {
        if (product == null) return null;

        return ProductCustomerResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discountPercent(product.getDiscountPercent())
                .category(ProductCustomerResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .slug(generateSlug(product.getCategory().getName()))
                        .build())
                .brand(ProductCustomerResponse.BrandInfo.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build())
                .images(product.getImages())
                .concentration(product.getConcentration())
                .volume(product.getCapacity())
                .originCountry(product.getCountryOfProduction())
                .inStock(product.isInStock())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .build();
    }

    public ProductCustomerDetailResponse toCustomerDetailResponse(Product product) {
        if (product == null) return null;

        return ProductCustomerDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .fullDescription(product.getFullDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .category(ProductCustomerResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .slug(generateSlug(product.getCategory().getName()))
                        .build())
                .brand(ProductCustomerDetailResponse.BrandDetailInfo.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .description(product.getBrand().getDescription())
                        .build())
                .images(product.getImages())
                .concentration(product.getConcentration())
                .volume(product.getCapacity())
                .originCountry(product.getCountryOfProduction())
                .originRegion(product.getOriginRegion())
                .vintageYear(product.getVintageYear())
                .grapeVariety(product.getGrapeVariety())
                .tasteProfile(parseTasteProfile(product.getTasteProfile()))
                .foodPairing(parseFoodPairing(product.getFoodPairing()))
                .servingTemperature(product.getServingTemperature())
                .inStock(product.isInStock())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .seller(buildSellerInfo(product))
                .build();
    }

    // ============= Seller View Mappings =============
    public ProductSellerResponse toSellerResponse(Product product) {
        if (product == null) return null;

        return ProductSellerResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .profitMargin(product.getProfitMargin())
                .originalPrice(product.getOriginalPrice())
                .category(ProductCustomerResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .brand(ProductCustomerResponse.BrandInfo.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build())
                .images(product.getImages())
                .concentration(product.getConcentration())
                .volume(product.getCapacity())
                .status(product.getStatus() != null ? product.getStatus().getCode() : null)
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .totalInventory(product.getTotalInventory())
                .inStock(product.isInStock())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .approvedAt(product.getApprovedAt())
                .build();
    }

    public ProductSellerDetailResponse toSellerDetailResponse(Product product) {
        if (product == null) return null;

        return ProductSellerDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .fullDescription(product.getFullDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .profitMargin(product.getProfitMargin())
                .originalPrice(product.getOriginalPrice())
                .category(ProductCustomerResponse.CategoryInfo.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .brand(ProductCustomerResponse.BrandInfo.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build())
                .images(product.getImages())
                .concentration(product.getConcentration())
                .volume(product.getCapacity())
                .originCountry(product.getCountryOfProduction())
                .status(product.getStatus() != null ? product.getStatus().getCode() : null)
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .inventory(buildInventoryInfo(product))
                .totalInventory(product.getTotalInventory())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .seo(ProductSellerDetailResponse.SeoInfo.builder()
                        .metaTitle(product.getMetaTitle())
                        .metaDescription(product.getMetaDescription())
                        .build())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .approvedAt(product.getApprovedAt())
                .build();
    }

    // ============= Entity Mappings =============
    public Product toEntity(WriteProductRequest request) {
        if (request == null) return null;

        return Product.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .concentration(request.getConcentration())
                .images(request.getImages())
                .capacity(request.getVolume())
                .ratingAverage(java.math.BigDecimal.ZERO)
                .ratingCount(0)
                .soldCount(0)
                .build();
    }

    public void updateEntity(Product product, WriteProductRequest request) {
        if (product == null || request == null) return;

        product.setName(request.getName());
        product.setSlug(generateSlug(request.getName()));
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setConcentration(request.getConcentration());
        product.setImages(request.getImages());
    }

    public WriteProductResponse toCreateResponse(Product product) {
        if (product == null) return null;

        return WriteProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .status(product.getStatus() != null ? product.getStatus().getCode() : null)
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }

    // ============= Helper Methods =============
    private String generateSlug(String name) {
        if (name == null) return null;
        return name.toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private ProductCustomerDetailResponse.TasteProfile parseTasteProfile(String tasteProfileJson) {
        if (tasteProfileJson == null || tasteProfileJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(tasteProfileJson,
                    ProductCustomerDetailResponse.TasteProfile.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse taste profile JSON: {}", tasteProfileJson, e);
            return null;
        }
    }

    private List<String> parseFoodPairing(String foodPairingJson) {
        if (foodPairingJson == null || foodPairingJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(foodPairingJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse food pairing JSON: {}", foodPairingJson, e);
            return new ArrayList<>();
        }
    }

    private List<ProductSellerDetailResponse.InventoryInfo> buildInventoryInfo(Product product) {
        if (product.getInventories() == null || product.getInventories().isEmpty()) {
            return new ArrayList<>();
        }

        return product.getInventories().stream()
                .map(inv -> ProductSellerDetailResponse.InventoryInfo.builder()
                        .warehouseId(inv.getWarehouse() != null ? inv.getWarehouse().getId() : null)
                        .warehouseName(inv.getWarehouse() != null ? inv.getWarehouse().getName() : null)
                        .quantity(inv.getQuantityOnHand())
                        .safetyStock(inv.getSafetyStock())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private ProductCustomerDetailResponse.SellerInfo buildSellerInfo(Product product) {
        if (product.getCreatedBy() == null || product.getCreatedBy().getUser() == null) {
            return null;
        }

        return ProductCustomerDetailResponse.SellerInfo.builder()
                .id(product.getCreatedBy().getId())
                .name(product.getCreatedBy().getUser().getFirstName() + " " +
                        product.getCreatedBy().getUser().getLastName())
                .rating(java.math.BigDecimal.valueOf(4.9)) // TODO: Calculate from reviews
                .build();
    }
}