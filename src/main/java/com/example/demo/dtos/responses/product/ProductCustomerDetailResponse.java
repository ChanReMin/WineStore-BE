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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCustomerDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String fullDescription;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductCustomerResponse.CategoryInfo category;
    private BrandDetailInfo brand;
    private List<ProductCustomerResponse.ImageInfo> images;
    private BigDecimal concentration;
    private Integer volume;
    private String originCountry;
    private String originRegion;
    private Integer vintageYear;
    private String grapeVariety;
    private TasteProfile tasteProfile;
    private List<String> foodPairing;
    private String servingTemperature;
    private Boolean inStock;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private Integer soldCount;
    private SellerInfo seller;

    @Getter
    @Setter
    @Builder
    public static class BrandDetailInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Getter
    @Setter
    @Builder
    public static class TasteProfile {
        private Integer sweetness;
        private Integer acidity;
        private Integer tannin;
        private Integer body;
    }

    @Getter
    @Setter
    @Builder
    public static class SellerInfo {
        private Long id;
        private String name;
        private BigDecimal rating;
    }
}