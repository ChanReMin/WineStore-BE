package com.example.demo.entities;

import com.example.demo.commons.enums.InventoryLogType;
import com.example.demo.utils.InventoryLogTypeConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_log", indexes = {
        @Index(name = "idx_warehouse_id", columnList = "warehouse_id"),
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "SMALLINT")
    @Convert(converter = InventoryLogTypeConverter.class)
    private InventoryLogType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String note;
}
