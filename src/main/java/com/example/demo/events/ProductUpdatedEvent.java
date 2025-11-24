package com.example.demo.events;

import com.example.demo.commons.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a product is updated
 * This event can trigger:
 * - Email notification to admin
 * - Update search index
 * - Clear cache for this product
 * - Log audit trail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdatedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private ProductStatus status;
    private BigDecimal price;

    // Category info
    private Long categoryId;
    private String categoryName;

    // Brand info
    private Long brandId;
    private String brandName;

    // Updater info
    private String updatedBy;
    private LocalDateTime updatedAt;
}
