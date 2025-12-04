package com.example.demo.entities;

import com.example.demo.commons.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "discount_type", nullable = false, columnDefinition = "SMALLINT")
    private DiscountType discountType; // 1=%, 2=fixed

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "used_count")
    private Integer usedCount;

    @Column(columnDefinition = "SMALLINT")
    private Integer status; // 1=active, 0=inactive

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Account createdBy;

    public void addPromotionProduct(PromotionProduct promotionProduct) {
        if (promotionProducts == null) {
            promotionProducts = new ArrayList<>();
        }

        promotionProducts.add(promotionProduct);
        promotionProduct.setPromotion(this);
    }

    public boolean isActive() {
        if (status == null || status != 1) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean canBeUsed() {
        return isActive() && (maxUsage == null || usedCount < maxUsage);
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!canBeUsed()) return BigDecimal.ZERO;

        if (discountType == DiscountType.PERCENTAGE) {
            return amount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            return discountValue.compareTo(amount) > 0 ? amount : discountValue;
        }
    }

    public BigDecimal calculateFinalPrice(BigDecimal amount) {
        if (!canBeUsed()) return amount;

        BigDecimal discount = calculateDiscount(amount);
        BigDecimal finalPrice = amount.subtract(discount);

        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}
