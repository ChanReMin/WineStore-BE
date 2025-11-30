package com.example.demo.repositories.queries;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountQueryRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    boolean existsByEmail(String email);
    Optional<Account> findByEmail(String email);
    boolean existsById(Long id);

    @Transactional(transactionManager = "writeTransactionManager")
    Account save(Account account);

    /**
     * Find account by ID with user - single query with JOIN FETCH
     */
    @Query("SELECT a FROM Account a " +
            "JOIN FETCH a.user u " +
            "WHERE a.id = :accountId")
    Optional<Account> findByIdWithUser(@Param("accountId") Long accountId);

    /**
     * Find account by email with user
     */
    @Query("SELECT a FROM Account a " +
            "JOIN FETCH a.user u " +
            "WHERE a.email = :email")
    Optional<Account> findByEmailWithUser(@Param("email") String email);

    /**
     * Get users list with filters - optimized with JOIN FETCH
     */
    @Query("SELECT a FROM Account a " +
            "JOIN FETCH a.user u " +
            "WHERE (:search IS NULL OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :search, '%')) " +
            "AND (:role IS NULL OR a.role = :role) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND a.role != com.example.demo.commons.enums.AccountRole.ADMIN")
    Page<Account> findUsersWithFilters(
            @Param("search") String search,
            @Param("role") AccountRole role,
            @Param("status") AccountStatus status,
            Pageable pageable
    );

    /**
     * Get user detail with all associations - single query
     */
    @Query("SELECT a FROM Account a " +
            "JOIN FETCH a.user u " +
            "LEFT JOIN FETCH u.addresses addr " +
            "WHERE a.id = :accountId")
    Optional<Account> findByIdWithUserAndAddresses(@Param("accountId") Long accountId);

    /**
     * Batch fetch accounts with users for bulk operations
     */
    @Query("SELECT a FROM Account a " +
            "JOIN FETCH a.user u " +
            "WHERE a.id IN :ids")
    List<Account> findAllByIdWithUser(@Param("ids") List<Long> ids);

    /**
     * Statistics queries
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.role != com.example.demo.commons.enums.AccountRole.ADMIN")
    Long countAllNonAdminUsers();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = :status AND a.role != com.example.demo.commons.enums.AccountRole.ADMIN")
    Long countByStatus(@Param("status") AccountStatus status);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.createdAt >= :startOfMonth AND a.role != com.example.demo.commons.enums.AccountRole.ADMIN")
    Long countNewUsersThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT a.role as role, COUNT(a) as count FROM Account a " +
            "WHERE a.role != com.example.demo.commons.enums.AccountRole.ADMIN " +
            "GROUP BY a.role")
    List<Object[]> countByRole();

    @Query("SELECT CAST(a.createdAt AS DATE) as date, COUNT(a) as count " +
            "FROM Account a " +
            "WHERE a.createdAt BETWEEN :startDate AND :endDate " +
            "AND a.role != com.example.demo.commons.enums.AccountRole.ADMIN " +
            "GROUP BY CAST(a.createdAt AS DATE) " +
            "ORDER BY CAST(a.createdAt AS DATE)")
    List<Object[]> getUserGrowth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
