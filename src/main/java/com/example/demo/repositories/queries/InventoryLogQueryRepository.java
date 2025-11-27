package com.example.demo.repositories.queries;

import com.example.demo.commons.enums.InventoryLogType;
import com.example.demo.entities.Account;
import com.example.demo.entities.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLogQueryRepository extends JpaRepository<InventoryLog, Long> {

    /**
     * Find inventory log by ID with details
     */
    @EntityGraph(attributePaths = {"warehouse", "product", "product.createdBy", "user", "user.account"})
    @Query("SELECT il FROM InventoryLog il WHERE il.id = :id AND il.deletedAt IS NULL")
    Optional<InventoryLog> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all inventory logs with filters (ADMIN view)
     */
    @EntityGraph(attributePaths = {"warehouse", "product", "user", "user.account"})
    @Query("SELECT il FROM InventoryLog il " +
            "WHERE (:warehouseId IS NULL OR il.warehouse.id = :warehouseId) " +
            "AND (:productId IS NULL OR il.product.id = :productId) " +
            "AND (:type IS NULL OR il.type = :type) " +
            "AND (:fromDate IS NULL OR il.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR il.createdAt <= :toDate) " +
            "AND il.deletedAt IS NULL")
    Page<InventoryLog> findAllWithFilters(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("type") InventoryLogType type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    /**
     * Find inventory logs for specific seller (filter by product createdBy)
     */
    @EntityGraph(attributePaths = {"warehouse", "product", "product.createdBy", "user", "user.account"})
    @Query("SELECT il FROM InventoryLog il " +
            "WHERE il.product.createdBy = :createdBy " +
            "AND (:warehouseId IS NULL OR il.warehouse.id = :warehouseId) " +
            "AND (:productId IS NULL OR il.product.id = :productId) " +
            "AND (:type IS NULL OR il.type = :type) " +
            "AND (:fromDate IS NULL OR il.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR il.createdAt <= :toDate) " +
            "AND il.deletedAt IS NULL")
    Page<InventoryLog> findAllByCreatedByWithFilters(
            @Param("createdBy") Account createdBy,
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("type") InventoryLogType type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    /**
     * Count by type (for summary)
     */
    @Query("SELECT COUNT(il) FROM InventoryLog il WHERE il.type = :type AND il.deletedAt IS NULL")
    Long countByType(@Param("type") InventoryLogType type);

    /**
     * Count by seller and type
     */
    @Query("SELECT COUNT(il) FROM InventoryLog il " +
            "WHERE il.product.createdBy = :createdBy " +
            "AND il.type = :type " +
            "AND il.deletedAt IS NULL")
    Long countByCreatedByAndType(
            @Param("createdBy") Account createdBy,
            @Param("type") InventoryLogType type
    );

    /**
     * Get last stock IN for a product in warehouse
     */
    @Query("SELECT il FROM InventoryLog il " +
            "WHERE il.product.id = :productId " +
            "AND il.warehouse.id = :warehouseId " +
            "AND il.type IN ('IN', 'RETURN', 'TRANSFER_IN') " +
            "AND il.deletedAt IS NULL " +
            "ORDER BY il.createdAt DESC")
    Optional<InventoryLog> findLastStockIn(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Get last stock OUT for a product in warehouse
     */
    @Query("SELECT il FROM InventoryLog il " +
            "WHERE il.product.id = :productId " +
            "AND il.warehouse.id = :warehouseId " +
            "AND il.type IN ('OUT', 'TRANSFER_OUT') " +
            "AND il.deletedAt IS NULL " +
            "ORDER BY il.createdAt DESC")
    Optional<InventoryLog> findLastStockOut(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    @EntityGraph(attributePaths = {"product", "user"})
    @Query("SELECT il FROM InventoryLog il " +
            "WHERE il.warehouse.id = :warehouseId " +
            "AND il.deletedAt IS NULL " +
            "ORDER BY il.createdAt DESC")
    List<InventoryLog> findTop10ByWarehouseIdOrderByCreatedAtDesc(@Param("warehouseId") Long warehouseId);
}