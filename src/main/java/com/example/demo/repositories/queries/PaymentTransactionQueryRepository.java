package com.example.demo.repositories.queries;

import com.example.demo.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface PaymentTransactionQueryRepository extends JpaRepository<PaymentTransaction, Long> {
    @Query("SELECT pm.code, COALESCE(SUM(pt.amount), 0), COUNT(DISTINCT pt.order.id) " +
            "FROM PaymentTransaction pt " +
            "JOIN pt.paymentMethod pm " +
            "JOIN pt.order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND pt.status = com.example.demo.commons.enums.TransactionStatus.SUCCESS " +
            "GROUP BY pm.code " +
            "ORDER BY SUM(pt.amount) DESC")
    List<Object[]> calculatePaymentMethodStats(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}
