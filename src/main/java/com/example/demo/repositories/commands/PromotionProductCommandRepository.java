package com.example.demo.repositories.commands;

import com.example.demo.entities.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionProductCommandRepository extends JpaRepository<PromotionProduct, Long> {
    boolean existsByPromotionIdAndProductId(Long promotionId, Long productId);

    @Query("SELECT pp FROM PromotionProduct pp " +
            "JOIN FETCH pp.promotion " +
            "JOIN FETCH pp.product " +
            "WHERE pp.promotion.id = :promotionId AND pp.product.id = :productId")
    Optional<PromotionProduct> findByPromotionIdAndProductId(
            @Param("promotionId") Long promotionId,
            @Param("productId") Long productId);

    /**
     * Fetch all promotions for a list of promotion IDs with JOIN FETCH to avoid N+1
     */
    @Query("SELECT pp FROM PromotionProduct pp " +
            "JOIN FETCH pp.promotion p " +
            "WHERE pp.promotion.id IN :promotionIds")
    List<PromotionProduct> findByPromotionIdIn(@Param("promotionIds") List<Long> promotionIds);
}
