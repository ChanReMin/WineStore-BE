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
public class ProductSellerResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal profitMargin;
    private BigDecimal originalPrice;
    private ProductCustomerResponse.CategoryInfo category;
    private ProductCustomerResponse.BrandInfo brand;
    private String images;
    private BigDecimal concentration;
    private Integer volume;
    private Integer status;
    private String statusText;
    private Integer totalInventory;
    private Boolean inStock;
    private Integer soldCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
}