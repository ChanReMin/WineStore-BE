package com.example.demo.dtos.responses.product;

import com.example.demo.commons.enums.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String wineType;
    private String countryOfProduction;
    private String grapeVariety;
    private BigDecimal concentration;
    private String productionArea;
    private Integer capacity;
    private String idealTemperature;
    private String humidity;
    private String avoidLight;
    private String placeTheBottleHorizontally;
    private String avoidVibration;
    private String openedWine;
    private String useWineCabinet;
    private String images;
    private String description;
    private ProductStatus status;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategoryInfo category;
    private BrandInfo brand;
    private AccountInfo approvedBy;
    private AccountInfo createdBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrandInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfo {
        private Long id;
        private String email;
        private String fullName;
    }
}
