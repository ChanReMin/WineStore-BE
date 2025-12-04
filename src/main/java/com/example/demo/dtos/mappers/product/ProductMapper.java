package com.example.demo.dtos.mappers.product;

import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.responses.product.*;
import com.example.demo.entities.Product;
import com.example.demo.entities.Promotion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .temperature(product.getIdealTemperature())
                .volume(product.getCapacity())
                .originCountry(product.getCountryOfProduction())
                .inStock(product.isInStock())
                .wineType(product.getWineType())
                .countryOfProduction(product.getCountryOfProduction())
                .humidity(product.getHumidity())
                .totalInventory(product.getTotalInventory())
                .light(product.getAvoidLight())
                .position(product.getPlaceTheBottleHorizontally())
                .vibration(product.getAvoidVibration())
                .afterOpening(product.getOpenedWine())
                .servingTemperature(product.getServingTemperature())
                .temperature(product.getIdealTemperature())
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
                .totalInventory(product.getTotalInventory())
                .grapeVariety(product.getGrapeVariety())
                .tasteProfile(parseTasteProfile(product.getTasteProfile()))
                .foodPairing(parseFoodPairing(product.getFoodPairing()))
                .servingTemperature(product.getServingTemperature())
                .wineType(product.getWineType())
                .temperature(product.getIdealTemperature())
                .humidity(product.getHumidity())
                .light(product.getAvoidLight())
                .position(product.getPlaceTheBottleHorizontally())
                .vibration(product.getAvoidVibration())
                .afterOpening(product.getOpenedWine())
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
                .countryOfProduction(product.getCountryOfProduction())
                .wineType(product.getWineType())
                .humidity(product.getHumidity())
                .light(product.getAvoidLight())
                .position(product.getPlaceTheBottleHorizontally())
                .vibration(product.getAvoidVibration())
                .afterOpening(product.getOpenedWine())
                .servingTemperature(product.getServingTemperature())
                .Temperature(product.getIdealTemperature())
                .inStock(product.isInStock())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .ratingCount(product.getRatingCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .approvedAt(product.getApprovedAt())
                .promotions(mapPromotions(product))
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
                .totalInventory(product.getTotalInventory())
                .wineType(product.getWineType())
                .humidity(product.getHumidity())
                .light(product.getAvoidLight())
                .position(product.getPlaceTheBottleHorizontally())
                .vibration(product.getAvoidVibration())
                .afterOpening(product.getOpenedWine())
                .totalInventory(product.getTotalInventory())
                .servingTemperature(product.getServingTemperature())
                .Temperature(product.getIdealTemperature())
                .soldCount(product.getSoldCount())
                .ratingAverage(product.getRatingAverage())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .approvedAt(product.getApprovedAt())
                .promotions(mapPromotions(product))
                .build();
    }

    // ============= Entity Mappings =============
    public Product toEntity(WriteProductRequest request) {
        if (request == null) return null;

        return Product.builder()
                .name(request.getName())
                .slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice())
                .wineType(request.getWinetype())
                .countryOfProduction(request.getCountryOfProduction())
                .grapeVariety(request.getGrapeVariety())
                .productionArea(request.getProductionArea())
                .idealTemperature(request.getIdealtemperature())
                .humidity(request.getHumidity())
                .avoidVibration(request.getAvoidVibration())
                .placeTheBottleHorizontally(request.getPlaceTheBottleHorizontally())
                .openedWine(request.getOpenedWine())
                .useWineCabinet(request.getUseWineCabinet())
                .concentration(request.getConcentration())
                .ratingAverage(java.math.BigDecimal.ZERO)
                .ratingCount(0)
                .soldCount(0)
                .build();
    }

    public void updateEntity(Product product, WriteProductRequest request) {
        if (product == null || request == null) return;

        product.setName(request.getName());
        product.setSlug(generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setConcentration(request.getConcentration());
        product.setWineType(request.getWinetype());
        product.setCountryOfProduction(request.getCountryOfProduction());
        product.setGrapeVariety(request.getGrapeVariety());
        product.setProductionArea(request.getProductionArea());
        product.setIdealTemperature(request.getIdealtemperature());
        product.setHumidity(request.getHumidity());
        product.setAvoidLight(request.getAvoidLight());
        product.setPlaceTheBottleHorizontally(request.getPlaceTheBottleHorizontally());
        product.setAvoidVibration(request.getAvoidVibration());
        product.setOpenedWine(request.getOpenedWine());
        product.setUseWineCabinet(request.getUseWineCabinet());
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

    private List<PromotionInfo> mapPromotions(Product product) {
        if (product.getPromotionProducts() == null || product.getPromotionProducts().isEmpty()) {
            return null;
        }

        return product.getPromotionProducts().stream()
                .filter(pp -> pp.getPromotion() != null && pp.getPromotion().getDeletedAt() == null)
                .map(pp -> {
                    Promotion promotion = pp.getPromotion();
                    BigDecimal calculatedDiscount = promotion.calculateDiscount(product.getPrice());

                    return PromotionInfo.builder()
                            .id(promotion.getId())
                            .code(promotion.getCode())
                            .name(promotion.getName())
                            .discountValue(calculatedDiscount)
                            .discountType(promotion.getDiscountType().name())
                            .build();
                })
                .collect(Collectors.toList());
    }
}