package com.example.demo.repositories.queries;

import com.example.demo.entities.Account;
import com.example.demo.entities.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryQueryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by ID with details
     */
    @EntityGraph(attributePaths = {"product", "product.createdBy", "warehouse", "warehouse.createdBy"})
    @Query("SELECT i FROM Inventory i WHERE i.id = :id AND i.deletedAt IS NULL")
    Optional<Inventory> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find inventory by product and warehouse
     */
    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId AND i.deletedAt IS NULL")
    Optional<Inventory> findByProductIdAndWarehouseId(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Find all inventory with filters (ADMIN view)
     */
    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT i FROM Inventory i " +
            "WHERE (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) " +
            "AND (:productId IS NULL OR i.product.id = :productId) " +
            "AND (:status IS NULL OR " +
            "   (:status = 'out_of_stock' AND i.quantityOnHand = 0) OR " +
            "   (:status = 'low_stock' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.safetyStock) OR " +
            "   (:status = 'in_stock' AND i.quantityOnHand > i.safetyStock)" +
            ") " +
            "AND (:search IS NULL OR :search = '' OR " +
            "   LOWER(i.product.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(i.product.sku) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") " +
            "AND i.deletedAt IS NULL")
    Page<Inventory> findAllWithFilters(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Find inventory by seller (filter by product.createdBy)
     */
    @EntityGraph(attributePaths = {"product", "product.createdBy", "warehouse"})
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.product.createdBy = :createdBy " +
            "AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) " +
            "AND (:productId IS NULL OR i.product.id = :productId) " +
            "AND (:status IS NULL OR " +
            "   (:status = 'out_of_stock' AND i.quantityOnHand = 0) OR " +
            "   (:status = 'low_stock' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.safetyStock) OR " +
            "   (:status = 'in_stock' AND i.quantityOnHand > i.safetyStock)" +
            ") " +
            "AND (:search IS NULL OR :search = '' OR " +
            "   LOWER(i.product.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "   LOWER(i.product.sku) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ") " +
            "AND i.deletedAt IS NULL")
    Page<Inventory> findAllByCreatedByWithFilters(
            @Param("createdBy") Account createdBy,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Find low stock inventory for alerts
     */
    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.quantityOnHand <= i.safetyStock " +
            "AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) " +
            "AND i.deletedAt IS NULL " +
            "ORDER BY i.quantityOnHand ASC")
    List<Inventory> findLowStockInventory(@Param("warehouseId") Long warehouseId);

    /**
     * Find low stock inventory by seller
     */
    @EntityGraph(attributePaths = {"product", "product.createdBy", "warehouse"})
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.product.createdBy = :createdBy " +
            "AND i.quantityOnHand <= i.safetyStock " +
            "AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId) " +
            "AND i.deletedAt IS NULL " +
            "ORDER BY i.quantityOnHand ASC")
    List<Inventory> findLowStockInventoryByCreatedBy(
            @Param("createdBy") Account createdBy,
            @Param("warehouseId") Long warehouseId
    );

    // ============= Count Queries for Summary =============

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.deletedAt IS NULL")
    Long countAllActive();

    @Query("SELECT COUNT(i) FROM Inventory i " +
            "WHERE i.product.createdBy = :createdBy AND i.deletedAt IS NULL")
    Long countByCreatedBy(@Param("createdBy") Account createdBy);

    @Query("SELECT COUNT(i) FROM Inventory i " +
            "WHERE (:status = 'out_of_stock' AND i.quantityOnHand = 0) OR " +
            "      (:status = 'low_stock' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.safetyStock) OR " +
            "      (:status = 'in_stock' AND i.quantityOnHand > i.safetyStock) " +
            "AND i.deletedAt IS NULL")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(i) FROM Inventory i " +
            "WHERE i.product.createdBy = :createdBy " +
            "AND ((:status = 'out_of_stock' AND i.quantityOnHand = 0) OR " +
            "     (:status = 'low_stock' AND i.quantityOnHand > 0 AND i.quantityOnHand <= i.safetyStock) OR " +
            "     (:status = 'in_stock' AND i.quantityOnHand > i.safetyStock)) " +
            "AND i.deletedAt IS NULL")
    Long countByCreatedByAndStatus(
            @Param("createdBy") Account createdBy,
            @Param("status") String status
    );

    // ============= Value Calculation =============

    @Query("SELECT COALESCE(SUM(i.quantityOnHand * i.product.costPrice), 0) " +
            "FROM Inventory i WHERE i.deletedAt IS NULL")
    BigDecimal calculateTotalValue();

    @Query("SELECT COALESCE(SUM(i.quantityOnHand * i.product.costPrice), 0) " +
            "FROM Inventory i " +
            "WHERE i.product.createdBy = :createdBy AND i.deletedAt IS NULL")
    BigDecimal calculateTotalValueByCreatedBy(@Param("createdBy") Account createdBy);

    /**
     * Count distinct products in warehouse
     */
    @Query("SELECT COUNT(DISTINCT i.product) FROM Inventory i " +
            "WHERE i.warehouse.id = :warehouseId AND i.deletedAt IS NULL")
    Integer countProductsInWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Count total inventory quantity in warehouse
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM Inventory i " +
            "WHERE i.warehouse.id = :warehouseId AND i.deletedAt IS NULL")
    Integer countTotalInventory(@Param("warehouseId") Long warehouseId);

    /**
     * Calculate total inventory value in warehouse
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand * i.product.costPrice), 0) " +
            "FROM Inventory i " +
            "WHERE i.warehouse.id = :warehouseId AND i.deletedAt IS NULL")
    BigDecimal calculateInventoryValue(@Param("warehouseId") Long warehouseId);

    /**
     * Count low stock products
     */
    @Query("SELECT COUNT(i) FROM Inventory i " +
            "WHERE i.warehouse.id = :warehouseId " +
            "AND i.safetyStock IS NOT NULL " +
            "AND i.quantityOnHand IS NOT NULL " +
            "AND i.quantityOnHand <= i.safetyStock " +
            "AND i.quantityOnHand > 0 " +
            "AND i.deletedAt IS NULL")
    Integer countLowStockProducts(@Param("warehouseId") Long warehouseId);

    /**
     * Count out of stock products
     */
    @Query("SELECT COUNT(i) FROM Inventory i " +
            "WHERE i.warehouse.id = :warehouseId " +
            "AND (i.quantityOnHand IS NULL OR i.quantityOnHand = 0) " +
            "AND i.deletedAt IS NULL")
    Integer countOutOfStockProducts(@Param("warehouseId") Long warehouseId);
}