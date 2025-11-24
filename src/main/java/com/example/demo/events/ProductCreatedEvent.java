package com.example.demo.events;

import com.example.demo.commons.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a new product is created
 * This event can trigger:
 * - Email notification to admin
 * - Update search index (Elasticsearch)
 * - Clear cache
 * - Analytics tracking
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreatedEvent implements Serializable {
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

    // Creator info
    private String createdBy;
    private LocalDateTime createdAt;
}

