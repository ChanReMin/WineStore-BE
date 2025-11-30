package com.example.demo.repositories.queries;

import com.example.demo.entities.Cart;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartQueryRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUser(User user);
}
