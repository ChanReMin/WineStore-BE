package com.example.demo.repositories.commands;

import com.example.demo.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartCommandRepository extends JpaRepository<Cart, Long> {
}
