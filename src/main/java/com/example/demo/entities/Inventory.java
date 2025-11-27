package com.example.demo.entities;

import com.example.demo.commons.enums.InventoryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_warehouse",
                        columnNames = {"product_id", "warehouse_id"})
        },
        indexes = {
                @Index(name = "idx_product_id", columnList = "product_id"),
                @Index(name = "idx_warehouse_id", columnList = "warehouse_id"),
                @Index(name = "idx_quantity", columnList = "quantity_on_hand")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_on_hand")
    private Integer quantityOnHand;

    @Column(name = "safety_stock")
    private Integer safetyStock;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    public void updateLastUpdated() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Check if inventory is low stock
     * Low stock = quantity > 0 AND quantity <= safetyStock
     */
    public boolean isLowStock() {
        return safetyStock != null && quantityOnHand != null
                && quantityOnHand > 0 && quantityOnHand <= safetyStock;
    }

    /**
     * Check if inventory is out of stock
     * Out of stock = quantity is null or <= 0
     */
    public boolean isOutOfStock() {
        return quantityOnHand == null || quantityOnHand <= 0;
    }

    /**
     * Get current inventory status
     * @return InventoryStatus enum
     */
    public InventoryStatus getStatus() {
        return InventoryStatus.calculateStatus(quantityOnHand, safetyStock);
    }

    /**
     * Get status as string code (for backward compatibility)
     * @return "in_stock", "low_stock", or "out_of_stock"
     */
    public String getStatusCode() {
        return getStatus().getCode();
    }
}