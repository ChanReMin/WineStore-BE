package com.example.demo.repositories.queries;

import com.example.demo.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodQueryRepository extends JpaRepository<PaymentMethod, Integer> {
    Optional<PaymentMethod> findByCode(String code);
}
