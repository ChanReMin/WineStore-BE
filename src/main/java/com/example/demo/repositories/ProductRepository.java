package com.example.demo.repositories;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "brand", "approvedBy"})
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    // Using EntityGraph instead of JOIN FETCH to avoid vector type issues
    @EntityGraph(attributePaths = {"category", "brand", "approvedBy"})
    @Query("SELECT p FROM Product p " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND p.deletedAt IS NULL")
    Page<Product> findAllWithFilters(
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    // Count by status
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.deletedAt IS NULL")
    Long countByStatus(@Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL")
    Long countAllActive();
}