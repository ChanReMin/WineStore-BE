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
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private List<ProductCustomerResponse.ImageInfo> images;
    private BigDecimal concentration;
    private Integer volume;
    private String originCountry;
    private Integer status;
    private String statusText;
    private List<InventoryInfo> inventory;
    private Integer totalInventory;
    private Integer soldCount;
    private BigDecimal ratingAverage;
    private SeoInfo seo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;

    @Getter
    @Setter
    @Builder
    public static class InventoryInfo {
        private Long warehouseId;
        private String warehouseName;
        private Integer quantity;
        private Integer safetyStock;
    }

    @Getter
    @Setter
    @Builder
    public static class SeoInfo {
        private String metaTitle;
        private String metaDescription;
    }
}
