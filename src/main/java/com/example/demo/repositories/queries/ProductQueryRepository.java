package com.example.demo.repositories.queries;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.entities.Account;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProductQueryRepository extends JpaRepository<Product, Long> {

    // ============= Seller Queries =============

    /**
     * Find products by seller with filters
     * Added concentrationFrom and concentrationTo filters
     */
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.createdBy = :createdBy " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:warehouseId IS NULL OR EXISTS ( " +
            "     SELECT inv FROM Inventory inv " +
            "     WHERE inv.product = p AND inv.warehouse.id = :warehouseId )) " +
            "AND (:priceFrom IS NULL OR p.price >= :priceFrom) " +
            "AND (:priceTo IS NULL OR p.price <= :priceTo) " +
            "AND (:concentrationFrom IS NULL OR p.concentration >= :concentrationFrom) " +
            "AND (:concentrationTo IS NULL OR p.concentration <= :concentrationTo) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllByCreatedByWithFilters(
            @Param("createdBy") Account createdBy,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("warehouseId") Long warehouseId,
            @Param("priceFrom") BigDecimal priceFrom,
            @Param("priceTo") BigDecimal priceTo,
            @Param("concentrationFrom") BigDecimal concentrationFrom,
            @Param("concentrationTo") BigDecimal concentrationTo,
            Pageable pageable
    );

    // ============= Admin Queries =============

    /**
     * Find all products with filters (Admin view)
     * Added concentrationFrom and concentrationTo filters
     */
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:warehouseId IS NULL OR EXISTS ( " +
            "     SELECT inv FROM Inventory inv " +
            "     WHERE inv.product = p AND inv.warehouse.id = :warehouseId )) " +
            "AND (:priceFrom IS NULL OR p.price >= :priceFrom) " +
            "AND (:priceTo IS NULL OR p.price <= :priceTo) " +
            "AND (:concentrationFrom IS NULL OR p.concentration >= :concentrationFrom) " +
            "AND (:concentrationTo IS NULL OR p.concentration <= :concentrationTo) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllWithFilters(
            @Param("status") ProductStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("warehouseId") Long warehouseId,
            @Param("priceFrom") BigDecimal priceFrom,
            @Param("priceTo") BigDecimal priceTo,
            @Param("concentrationFrom") BigDecimal concentrationFrom,
            @Param("concentrationTo") BigDecimal concentrationTo,
            Pageable pageable
    );

    // ============= Customer/Guest Queries =============

    /**
     * Find active products for customers (only ACTIVE status)
     * Added concentrationFrom and concentrationTo filters
     */
    @EntityGraph(attributePaths = {"category", "brand", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.status = com.example.demo.commons.enums.ProductStatus.ACTIVE " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.category.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:warehouseId IS NULL OR EXISTS ( " +
            "     SELECT inv FROM Inventory inv " +
            "     WHERE inv.product = p AND inv.warehouse.id = :warehouseId )) " +
            "AND (:priceFrom IS NULL OR p.price >= :priceFrom) " +
            "AND (:priceTo IS NULL OR p.price <= :priceTo) " +
            "AND (:concentrationFrom IS NULL OR p.concentration >= :concentrationFrom) " +
            "AND (:concentrationTo IS NULL OR p.concentration <= :concentrationTo) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllActiveProductsWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("warehouseId") Long warehouseId,
            @Param("priceFrom") BigDecimal priceFrom,
            @Param("priceTo") BigDecimal priceTo,
            @Param("concentrationFrom") BigDecimal concentrationFrom,
            @Param("concentrationTo") BigDecimal concentrationTo,
            Pageable pageable
    );

    /**
     * Find related products (same category, active status)
     */
    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.category = :category " +
            "AND p.status = :status " +
            "AND p.id != :excludeId " +
            "AND p.deletedAt IS NULL " +
            "ORDER BY p.soldCount DESC, p.ratingAverage DESC")
    List<Product> findTop10ByCategoryAndStatusAndIdNotAndDeletedAtIsNull(
            @Param("category") Category category,
            @Param("status") ProductStatus status,
            @Param("excludeId") Long excludeId
    );

    // ============= Count Queries =============

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy = :createdBy AND p.deletedAt IS NULL")
    Long countByCreatedBy(@Param("createdBy") Account createdBy);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy = :createdBy AND p.status = :status AND p.deletedAt IS NULL")
    Long countByCreatedByAndStatus(@Param("createdBy") Account createdBy, @Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.deletedAt IS NULL")
    Long countByStatus(@Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL")
    Long countAllActive();
    // Custom method to find products by category IDs
    List<Product> findByCategoryIdIn(Collection<Long> categoryIds);

}