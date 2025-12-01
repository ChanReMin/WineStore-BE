package com.example.demo.dtos.responses.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude()
public class ProductSellerDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private String fullDescription;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal profitMargin;
    private BigDecimal originalPrice;
    private ProductCustomerResponse.CategoryInfo category;
    private ProductCustomerResponse.BrandInfo brand;
    private String images;
    private BigDecimal concentration;
    private Integer volume;
    private String originCountry;
    private Integer status;
    private String statusText;
    private Integer totalInventory;
    private Integer soldCount;
    private String wineType;
    private String humidity;
    private String light;
    private String position;
    private String vibration;
    private String afterOpening;
    private String servingTemperature;
    private String Temperature;
    private BigDecimal ratingAverage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;

    private List<PromotionInfo> promotions;
}
