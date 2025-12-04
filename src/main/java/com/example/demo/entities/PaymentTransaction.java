package com.example.demo.entities;

import com.example.demo.commons.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_payment_method_id", columnList = "payment_method_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_provider_txn_code", columnList = "provider_txn_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false, columnDefinition = "SMALLINT")
    private TransactionStatus status;

    @Column(name = "provider_txn_code", length = 200)
    private String providerTxnCode; // Mã giao dịch từ payment provider

    @Column(name = "responsedata" , columnDefinition = "TEXT")
    private String responseData; // JSON response từ provider
}
