package com.example.demo.repositories.commands;

import com.example.demo.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodCommandRepository extends JpaRepository<PaymentMethod, Long> {
}
