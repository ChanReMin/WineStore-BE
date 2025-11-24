package com.example.demo.listeners;

import com.example.demo.events.ProductCreatedEvent;
import com.example.demo.events.ProductUpdatedEvent;
import com.example.demo.events.ProductDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Product Event Listener
 *
 * Listens to product events from RabbitMQ and performs side effects:
 * - Email notifications
 * - Cache management
 * - Search index updates (Elasticsearch)
 * - Analytics tracking
 * - Audit logging
 *
 * NOTE: These listeners do NOT sync data to Read DB!
 * PostgreSQL Logical Replication handles that automatically.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {
    /**
     * Handle ProductCreatedEvent
     * Triggered when a new product is created
     */
    @RabbitListener(queues = "product.queue")
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("📩 Received ProductCreatedEvent: Product ID={}, Name={}, Status={}, CreatedBy={}",
                event.getProductId(),
                event.getProductName(),
                event.getStatus(),
                event.getCreatedBy());

        try {
            // 1. Send email notification to admin
            sendAdminNotification(event);

            // 2. Update search index (Elasticsearch)
            updateSearchIndex(event);

            // 3. Clear cache (if any)
            clearProductCache();

            // 4. Track analytics
            trackProductCreated(event);

            // 5. Log to audit trail
            logAuditTrail("PRODUCT_CREATED", event.getProductId(), event.getCreatedBy());

            log.info("✅ Successfully processed ProductCreatedEvent for product ID: {}",
                    event.getProductId());

        } catch (Exception e) {
            log.error("❌ Error processing ProductCreatedEvent for product ID: {}. Error: {}",
                    event.getProductId(), e.getMessage(), e);
            // Don't throw exception - let RabbitMQ handle retry
        }
    }

    /**
     * Handle ProductUpdatedEvent
     * Triggered when a product is updated
     */
    @RabbitListener(queues = "product.queue")
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("📩 Received ProductUpdatedEvent: Product ID={}, Name={}, Status={}, UpdatedBy={}",
                event.getProductId(),
                event.getProductName(),
                event.getStatus(),
                event.getUpdatedBy());

        try {
            // 1. Send email notification to admin
            sendAdminNotification(event);

            // 2. Update search index
            updateSearchIndex(event);

            // 3. Clear cache for this specific product
            clearProductCacheById(event.getProductId());

            // 4. Track analytics
            trackProductUpdated(event);

            // 5. Log to audit trail
            logAuditTrail("PRODUCT_UPDATED", event.getProductId(), event.getUpdatedBy());

            log.info("✅ Successfully processed ProductUpdatedEvent for product ID: {}",
                    event.getProductId());

        } catch (Exception e) {
            log.error("❌ Error processing ProductUpdatedEvent for product ID: {}. Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }

    /**
     * Handle ProductDeletedEvent
     * Triggered when a product is deleted (soft delete)
     */
    @RabbitListener(queues = "product.queue")
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("📩 Received ProductDeletedEvent: Product ID={}, Name={}, DeletedBy={}",
                event.getProductId(),
                event.getProductName(),
                event.getDeletedBy());

        try {
            // 1. Send email notification to admin
            sendAdminNotificationForDeletion(event);

            // 2. Remove from search index
            removeFromSearchIndex(event.getProductId());

            // 3. Clear cache
            clearProductCacheById(event.getProductId());

            // 4. Track analytics
            trackProductDeleted(event);

            // 5. Log to audit trail
            logAuditTrail("PRODUCT_DELETED", event.getProductId(), event.getDeletedBy());

            log.info("✅ Successfully processed ProductDeletedEvent for product ID: {}",
                    event.getProductId());

        } catch (Exception e) {
            log.error("❌ Error processing ProductDeletedEvent for product ID: {}. Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }


    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    /**
     * Send email notification to admin
     */
    private void sendAdminNotification(ProductCreatedEvent event) {
        // TODO: Implement email service
        log.info("📧 [TODO] Send email to admin: New product created - {}", event.getProductName());

        // Example implementation:
        // emailService.sendEmail(
        //     "admin@example.com",
        //     "New Product Created",
        //     String.format("Product '%s' has been created by %s",
        //         event.getProductName(), event.getCreatedBy())
        // );
    }

    private void sendAdminNotification(ProductUpdatedEvent event) {
        log.info("📧 [TODO] Send email to admin: Product updated - {}", event.getProductName());
    }

    private void sendAdminNotificationForDeletion(ProductDeletedEvent event) {
        log.info("📧 [TODO] Send email to admin: Product deleted - {}", event.getProductName());
    }

    /**
     * Update search index (Elasticsearch)
     */
    private void updateSearchIndex(ProductCreatedEvent event) {
        // TODO: Implement Elasticsearch service
        log.info("🔍 [TODO] Update search index for product ID: {}", event.getProductId());

        // Example implementation:
        // elasticsearchService.indexProduct(
        //     event.getProductId(),
        //     event.getProductName(),
        //     event.getCategoryName(),
        //     event.getBrandName()
        // );
    }

    private void updateSearchIndex(ProductUpdatedEvent event) {
        log.info("🔍 [TODO] Update search index for product ID: {}", event.getProductId());
    }


    private void removeFromSearchIndex(Long productId) {
        // TODO: Implement Elasticsearch service
        log.info("🔍 [TODO] Remove from search index: product ID {}", productId);

        // Example implementation:
        // elasticsearchService.removeProduct(productId);
    }

    /**
     * Clear cache
     */
    private void clearProductCache() {
        // TODO: Implement Redis cache service
        log.info("💾 [TODO] Clear all product cache");

        // Example implementation:
        // cacheService.evictCache("products");
    }

    private void clearProductCacheById(Long productId) {
        // TODO: Implement Redis cache service
        log.info("💾 [TODO] Clear cache for product ID: {}", productId);

        // Example implementation:
        // cacheService.evictCache("product:" + productId);
    }

    /**
     * Track analytics
     */
    private void trackProductCreated(ProductCreatedEvent event) {
        // TODO: Implement analytics service
        log.info("📊 [TODO] Track product created: ID={}, Category={}, Brand={}",
                event.getProductId(), event.getCategoryName(), event.getBrandName());

        // Example implementation:
        // analyticsService.track("product_created", Map.of(
        //     "product_id", event.getProductId(),
        //     "category", event.getCategoryName(),
        //     "brand", event.getBrandName()
        // ));
    }

    private void trackProductUpdated(ProductUpdatedEvent event) {
        log.info("📊 [TODO] Track product updated: ID={}", event.getProductId());
    }

    private void trackProductDeleted(ProductDeletedEvent event) {
        log.info("📊 [TODO] Track product deleted: ID={}", event.getProductId());
    }

    /**
     * Log to audit trail
     */
    private void logAuditTrail(String action, Long productId, String performedBy) {
        // TODO: Implement audit log service
        log.info("📝 [TODO] Audit log: Action={}, ProductID={}, PerformedBy={}",
                action, productId, performedBy);

    }
}