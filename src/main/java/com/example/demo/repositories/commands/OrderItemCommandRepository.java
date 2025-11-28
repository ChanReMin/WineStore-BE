package com.example.demo.repositories.commands;

import com.example.demo.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemCommandRepository extends JpaRepository<OrderItem, Long> {
}
