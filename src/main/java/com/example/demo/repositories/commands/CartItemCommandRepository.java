package com.example.demo.repositories.commands;

import com.example.demo.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemCommandRepository extends JpaRepository<CartItem, Long> {
}
