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
import java.util.List;

@Repository
public interface ProductQueryRepository extends JpaRepository<Product, Long> {

    // ============= Seller Queries =============

    /**
     * Find products by seller with filters
     */
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.createdBy = :createdBy " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllByCreatedByWithFilters(
            @Param("createdBy") Account createdBy,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // ============= Admin Queries =============

    /**
     * Find all products with filters (Admin view)
     */
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllWithFilters(
            @Param("status") ProductStatus status,
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // ============= Customer/Guest Queries =============

    /**
     * Find active products for customers (only ACTIVE status)
     */
    @EntityGraph(attributePaths = {"category", "brand", "inventories"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.status = com.example.demo.commons.enums.ProductStatus.ACTIVE " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:inStock IS NULL OR :inStock = FALSE OR EXISTS (" +
            "    SELECT 1 FROM Inventory i WHERE i.product = p AND i.quantityOnHand > 0" +
            ")) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllActiveProductsWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
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
}