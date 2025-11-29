package com.example.demo.repositories.commands;

import com.example.demo.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionCommandRepository extends JpaRepository<Promotion, Long> {
    boolean existsByCode(String code);

}
