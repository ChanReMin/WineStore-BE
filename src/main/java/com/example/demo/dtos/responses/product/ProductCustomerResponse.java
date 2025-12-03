package com.example.demo.dtos.responses.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude()
public class ProductCustomerResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private CategoryInfo category;
    private BrandInfo brand;
    private String images;
    private BigDecimal concentration;
    private Integer volume; // capacity
    private String originCountry;
    private String countryOfProduction;
    private Integer totalInventory;
    private String servingTemperature;
    private String temperature;
    private String wineType;
    private String humidity;
    private String light;
    private String position;
    private String vibration;
    private String afterOpening;
    private Boolean inStock;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private Integer soldCount;

    @Getter
    @Setter
    @Builder
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
    }

    @Getter
    @Setter
    @Builder
    public static class BrandInfo {
        private Long id;
        private String name;
    }
}
