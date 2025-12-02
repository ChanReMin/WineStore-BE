package com.example.demo.repositories.queries;
import com.example.demo.commons.enums.WarehouseStatus;
import com.example.demo.entities.Account;
import com.example.demo.entities.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseQueryRepository extends JpaRepository<Warehouse, Long> {

    /**
     * Find warehouse by ID with details
     */
    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.id = :id AND w.deletedAt IS NULL")
    Optional<Warehouse> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all active warehouses
     */
    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.deletedAt IS NULL ORDER BY w.createdAt DESC")
    List<Warehouse> findAllActive();

    /**
     * Find warehouses by manager (for SELLER)
     */
    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.createdBy = :manager AND w.deletedAt IS NULL")
    List<Warehouse> findByManager(@Param("manager") Account manager);

    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.createdBy = :manager AND w.deletedAt IS NULL")
    Page<Warehouse> findByManager(@Param("manager") Account manager, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.createdBy = :manager AND w.status = :status AND w.deletedAt IS NULL")
    Page<Warehouse> findByManagerAndStatus(
            @Param("manager") Account manager,
            @Param("status") WarehouseStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.createdBy = :manager " +
            "AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(w.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND w.deletedAt IS NULL")
    Page<Warehouse> findByManagerWithSearch(
            @Param("manager") Account manager,
            @Param("search") String search,
            Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE w.createdBy = :manager AND w.status = :status " +
            "AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(w.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND w.deletedAt IS NULL")
    Page<Warehouse> findByManagerAndStatusWithSearch(
            @Param("manager") Account manager,
            @Param("status") WarehouseStatus status,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Admin queries with filters
     */
    @EntityGraph(attributePaths = {"createdBy", "createdBy.user"})
    @Query("SELECT w FROM Warehouse w WHERE " +
            "(:status IS NULL OR w.status = :status) AND " +
            "(:managerId IS NULL OR w.createdBy.id = :managerId) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(w.location) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "w.deletedAt IS NULL")
    Page<Warehouse> findAllWithFilters(
            @Param("status") WarehouseStatus status,
            @Param("managerId") Long managerId,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Count queries
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.createdBy.id = :managerId AND w.deletedAt IS NULL")
    long countByManager(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.createdBy.id = :managerId " +
            "AND w.status = :status AND w.deletedAt IS NULL")
    long countByManagerAndStatus(@Param("managerId") Long managerId, @Param("status") WarehouseStatus status);

    @Query("SELECT DISTINCT w.city FROM Warehouse w " +
            "WHERE w.city IS NOT NULL " +
            "AND w.city <> '' " +
            "AND w.deletedAt IS NULL " +
            "ORDER BY w.city ASC")
    List<String> findDistinctCities();

    /**
     * Tìm warehouses theo city
     * Chỉ lấy các warehouse chưa bị xóa
     */
    @Query("SELECT w FROM Warehouse w " +
            "WHERE w.city = :city " +
            "AND w.deletedAt IS NULL " +
            "ORDER BY w.createdAt DESC")
    List<Warehouse> findByCity(@Param("city") String city);
}