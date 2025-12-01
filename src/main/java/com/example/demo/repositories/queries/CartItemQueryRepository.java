package com.example.demo.repositories.queries;

import com.example.demo.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemQueryRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p LEFT JOIN FETCH p.inventories WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartId(Long cartId);
}
