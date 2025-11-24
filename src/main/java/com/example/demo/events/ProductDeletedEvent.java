package com.example.demo.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event fired when a product is deleted (soft delete)
 * This event can trigger:
 * - Email notification to admin
 * - Remove from search index
 * - Clear cache
 * - Log audit trail
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDeletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private String deletedBy;
    private LocalDateTime deletedAt;
}
