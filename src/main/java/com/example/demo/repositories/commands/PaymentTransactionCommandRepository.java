package com.example.demo.repositories.commands;

import com.example.demo.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionCommandRepository extends JpaRepository<PaymentTransaction, Long> {
}
