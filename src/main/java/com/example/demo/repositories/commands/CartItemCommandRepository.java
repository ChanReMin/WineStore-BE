package com.example.demo.repositories.commands;

import com.example.demo.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemCommandRepository extends JpaRepository<CartItem, Long> {
    @Query(value = "SELECT * FROM cart_items WHERE cart_id = :cartId AND product_id = :productId",
            nativeQuery = true)
    Optional<CartItem> findByCartIdAndProductIdIncludingDeleted(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId);
}
