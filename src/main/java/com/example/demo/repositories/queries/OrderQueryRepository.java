package com.example.demo.repositories.queries;

import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderQueryRepository extends JpaRepository<Order, Long> {

    /**
     * Đếm tổng số đơn hàng từ một warehouse
     * Logic: Đếm các order có orderItems chứa products từ warehouse này
     */
    @Query("""
        SELECT COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.orderItems oi
        JOIN oi.product p
        JOIN Inventory inv ON inv.product.id = p.id
        WHERE inv.warehouse.id = :warehouseId
        AND o.deletedAt IS NULL
    """)
    Long countOrdersFromWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Tính tổng doanh thu từ các đơn hàng của warehouse
     * Logic: Tính tổng giá trị của các orderItems có products từ warehouse này
     */
    @Query("""
        SELECT COALESCE(SUM(oi.unitPrice * oi.quantity), 0)
        FROM OrderItem oi
        JOIN oi.product p
        JOIN Inventory inv ON inv.product.id = p.id
        WHERE inv.warehouse.id = :warehouseId
        AND oi.order.deletedAt IS NULL
    """)
    BigDecimal calculateRevenueFromWarehouse(@Param("warehouseId") Long warehouseId);

    /**
     * Đếm số đơn hàng từ warehouse trong 30 ngày qua
     */
    @Query("""
        SELECT COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.orderItems oi
        JOIN oi.product p
        JOIN Inventory inv ON inv.product.id = p.id
        WHERE inv.warehouse.id = :warehouseId
        AND o.createdAt >= :fromDate
        AND o.deletedAt IS NULL
    """)
    Long countOrdersFromWarehouseSince(
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDateTime fromDate
    );

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.status NOT IN (com.example.demo.commons.enums.OrderStatus.CANCELLED)")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
            "WHERE o.status NOT IN (com.example.demo.commons.enums.OrderStatus.CANCELLED)")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") OrderStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countUniqueCustomers(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    Long countByUserId(@Param("userId") Long userId);

    Long countByStatus(@Param("status") OrderStatus status);

    @Query(value = """
        SELECT
            CASE
                WHEN :period = 'daily' THEN TO_CHAR(o.created_at, 'YYYY-MM-DD')
                WHEN :period = 'weekly' THEN TO_CHAR(DATE_TRUNC('week', o.created_at), 'YYYY-MM-DD')
                WHEN :period = 'monthly' THEN TO_CHAR(DATE_TRUNC('month', o.created_at), 'YYYY-MM')
            END as period_label,
            COALESCE(SUM(o.final_amount), 0) as total_revenue,
            COUNT(o.id) as total_orders
        FROM orders o
        WHERE o.status NOT IN (6) -- Assuming 6 is CANCELLED status (replace with enum value if possible)
        AND o.created_at BETWEEN :startDate AND :endDate
        GROUP BY period_label
        ORDER BY period_label
    """, nativeQuery = true)
    List<Object[]> findRevenueAndOrdersByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("period") String period
    );

    // Category Performance
    @Query("SELECT c.id, c.name, COALESCE(SUM(o.finalAmount), 0), COUNT(o) " +
            "FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN oi.product p " +
            "JOIN p.category c " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.status NOT IN (com.example.demo.commons.enums.OrderStatus.CANCELLED) " +
            "GROUP BY c.id, c.name " +
            "ORDER BY SUM(o.finalAmount) DESC")
    List<Object[]> calculateCategoryPerformance(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Region Performance
    @Query("SELECT ua.city, COALESCE(SUM(o.finalAmount), 0), COUNT(o) " +
            "FROM Order o " +
            "JOIN o.shippingAddress ua " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.status NOT IN (com.example.demo.commons.enums.OrderStatus.CANCELLED) " +
            "GROUP BY ua.city " +
            "ORDER BY SUM(o.finalAmount) DESC")
    List<Object[]> calculateRegionPerformance(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " + // Fetch product for each order item
            "LEFT JOIN FETCH o.shippingAddress " +
            "LEFT JOIN FETCH o.paymentTransactions pt " +
            "LEFT JOIN FETCH pt.paymentMethod " + // Fetch payment method for each transaction
            "WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"orderItems", "shippingAddress", "user", "user.account"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
}