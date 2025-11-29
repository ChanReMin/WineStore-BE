package com.example.demo.repositories.queries;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.entities.Account;
import com.example.demo.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductQueryRepository extends JpaRepository<Product, Long>{
    // Queries for seller - filter by createdBy
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy"})
    @Query("SELECT p FROM Product p " +
            "WHERE p.createdBy = :createdBy " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllByCreatedByWithFilters(
            @Param("createdBy") Account createdBy,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    // Using EntityGraph instead of JOIN FETCH to avoid vector type issues
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy", "createdBy"})
    @Query("SELECT p FROM Product p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllWithFilters(
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy = :createdBy AND p.deletedAt IS NULL")
    Long countByCreatedBy(@Param("createdBy") Account createdBy);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy = :createdBy AND p.status = :status AND p.deletedAt IS NULL")
    Long countByCreatedByAndStatus(@Param("createdBy") Account createdBy, @Param("status") ProductStatus status);

    // Count by status
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.deletedAt IS NULL")
    Long countByStatus(@Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL")
    Long countAllActive();

    // Custom method to find products by category IDs
    List<Product> findByCategoryIdIn(Collection<Long> categoryIds);

}
