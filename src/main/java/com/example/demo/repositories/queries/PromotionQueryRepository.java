package com.example.demo.repositories.queries;

import com.example.demo.entities.Promotion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionQueryRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    @EntityGraph(attributePaths = {"promotionProducts", "promotionProducts.product", "promotionProducts.product.category"})
    Optional<Promotion> findById(Long id);

    Optional<Promotion> findByCode(String code);
}