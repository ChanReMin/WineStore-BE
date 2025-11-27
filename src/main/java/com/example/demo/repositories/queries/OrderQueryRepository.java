package com.example.demo.repositories.queries;

import com.example.demo.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}