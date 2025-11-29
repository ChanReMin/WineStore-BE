package com.example.demo.repositories.commands;

import com.example.demo.entities.Promotion;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionCommandRepository extends JpaRepository<Promotion, Long> {
    boolean existsByCode(String code);

    List<Promotion> findAllByIdIn(@NotEmpty(message = "Promotion IDs cannot be empty") List<Long> promotionIds);
}
