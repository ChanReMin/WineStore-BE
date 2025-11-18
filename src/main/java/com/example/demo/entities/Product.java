package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "product",
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
        @Index(name = "idx_created_year", columnList = "created_at")
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

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "wine_type", length = 100)
    private String wineType; // Vang đỏ, Vang trắng, etc.

    @Column(name = "country_of_production", length = 100)
    private String countryOfProduction;

    @Column(name = "grape_variety", length = 200)
    private String grapeVariety;

    @Column(precision = 5, scale = 2)
    private BigDecimal concentration; // % alcohol

    @Column(name = "production_area", length = 200)
    private String productionArea;

    @Column(columnDefinition = "INTEGER")
    private Integer capacity; // ml

    @Column(name = "ideal_temperature", columnDefinition = "TEXT")
    private String idealTemperature;

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

    @Column(length = 1000)
    private String images; // JSON string hoặc comma-separated URLs

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product")
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

//    @ManyToMany(mappedBy = "products")
//    private java.util.List<Promotion> promotions;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts;
}
