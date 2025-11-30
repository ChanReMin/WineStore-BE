package com.example.demo.entities;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.utils.FloatArrayToVectorConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_name_deleted_at",
                        columnNames = {"name", "deleted_at"}
                )
        },
        indexes = {
                @Index(name = "idx_category_id", columnList = "category_id"),
                @Index(name = "idx_brand_id", columnList = "brand_id"),
                @Index(name = "idx_wine_type", columnList = "wine_type"),
                @Index(name = "idx_created_year", columnList = "created_at"),
                @Index(name = "idx_description_vector", columnList = "description_vector"),
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(length = 200)
    private String slug;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "wine_type", length = 100)
    private String wineType;

    @Column(name = "country_of_production", length = 100)
    private String countryOfProduction;

    @Column(name = "origin_region", length = 200)
    private String originRegion;

    @Column(name = "grape_variety", length = 200)
    private String grapeVariety;

    @Column(precision = 5, scale = 2)
    private BigDecimal concentration;

    @Column(name = "production_area", length = 200)
    private String productionArea;

    @Column(name = "vintage_year")
    private Integer vintageYear;

    @Column(columnDefinition = "INTEGER")
    private Integer capacity;

    @Column(name = "ideal_temperature", columnDefinition = "TEXT")
    private String idealTemperature;

    @Column(name = "serving_temperature", length = 50)
    private String servingTemperature;

    @Column(columnDefinition = "TEXT")
    private String humidity;

    @Column(name = "avoid_light", columnDefinition = "TEXT")
    private String avoidLight;

    @Column(name = "place_the_bottle_horizontally", columnDefinition = "TEXT")
    private String placeTheBottleHorizontally;

    @Column(name = "avoid_vibration", columnDefinition = "TEXT")
    private String avoidVibration;

    @Column(name = "opened_wine", columnDefinition = "TEXT")
    private String openedWine;

    @Column(name = "use_wine_cabinet", columnDefinition = "TEXT")
    private String useWineCabinet;

    @Column(name = "food_pairing", columnDefinition = "TEXT")
    private String foodPairing; // JSON array: ["Thịt bò", "Pho mát"]

    @Column(name = "taste_profile", columnDefinition = "TEXT")
    private String tasteProfile; // JSON object: {"sweetness": 2, "acidity": 7}

    @Column(length = 1000)
    private String images;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "description_vector", columnDefinition = "vector(384)", insertable = false, updatable = false)
    @Convert(converter = FloatArrayToVectorConverter.class)
    @Basic(fetch = FetchType.LAZY, optional = true)
    private float[] descriptionVector;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "SMALLINT", nullable = false)
    private ProductStatus status;

    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "sold_count")
    private Integer soldCount;

    // SEO fields
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @OneToMany(mappedBy = "product")
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts;


    /**
     * Calculate profit margin percentage
     */
    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(costPrice)
                .divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate discount percentage
     */
    public BigDecimal getDiscountPercent() {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(price)
                .divide(originalPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        if (inventories == null || inventories.isEmpty()) {
            return false;
        }
        return inventories.stream()
                .mapToInt(inv -> inv.getQuantityOnHand() != null ? inv.getQuantityOnHand() : 0)
                .sum() > 0;
    }

    /**
     * Get total inventory quantity
     */
    public Integer getTotalInventory() {
        if (inventories == null || inventories.isEmpty()) {
            return 0;
        }
        return inventories.stream()
                .mapToInt(inv -> inv.getQuantityOnHand() != null ? inv.getQuantityOnHand() : 0)
                .sum();
    }
}