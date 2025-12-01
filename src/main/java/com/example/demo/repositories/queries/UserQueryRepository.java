package com.example.demo.repositories.queries;

import com.example.demo.entities.User;
import com.example.demo.entities.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQueryRepository extends JpaRepository<User, Long> {
    List<User> findByFirstNameContaining(String name);

    @Query("SELECT u FROM User u JOIN FETCH u.account WHERE u.account.id = :accountId")
    Optional<User> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Batch queries for statistics - AVOID N+1
     */
    @Query("SELECT o.user.id as userId, COUNT(o) as orderCount " +
            "FROM Order o " +
            "WHERE o.user.id IN :userIds " +
            "GROUP BY o.user.id")
    List<Object[]> countOrdersByUserIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT o.user.id as userId, COALESCE(SUM(o.totalAmount), 0) as totalSpent " +
            "FROM Order o " +
            "WHERE o.user.id IN :userIds " +
            "GROUP BY o.user.id")
    List<Object[]> sumTotalSpentByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Single user queries for detail page
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Integer countOrdersByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId")
    Long sumTotalSpentByUserId(@Param("userId") Long userId);

    /**
     * Product count for sellers
     * Uses createdBy (Account) instead of seller (User)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdBy.id = :accountId")
    Integer countProductsByAccountId(@Param("accountId") Long accountId);

    /**
     * Batch query for product counts - AVOID N+1
     * Uses createdBy (Account) instead of seller (User)
     */
    @Query("SELECT p.createdBy.id as accountId, COUNT(p) as productCount " +
            "FROM Product p " +
            "WHERE p.createdBy.id IN :accountIds " +
            "GROUP BY p.createdBy.id")
    List<Object[]> countProductsByAccountIds(@Param("accountIds") List<Long> accountIds);

    @Query("SELECT ua FROM UserAddress ua " +
            "WHERE ua.user.id = :userId " +
            "AND ua.deletedAt IS NULL " +
            "ORDER BY ua.isDefault DESC, ua.createdAt DESC")
    List<UserAddress> findAddressesByUserId(@Param("userId") Long userId);

    @Query("SELECT ua FROM UserAddress ua " +
            "WHERE ua.id = :addressId " +
            "AND ua.deletedAt IS NULL")
    Optional<UserAddress> findAddressById(@Param("addressId") Long addressId);
}
