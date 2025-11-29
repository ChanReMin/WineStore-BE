package com.example.demo.repositories.commands;

import com.example.demo.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionCommandRepository extends JpaRepository<Promotion, Long> {
    /**
     * Fetch promotions by IDs in batch to avoid N+1 query problem
     * Returns only the fields we need: id, code, name, status, startDate, endDate
     */
    @Query("SELECT p FROM Promotion p WHERE p.id IN :ids")
    List<Promotion> findAllByIdIn(@Param("ids") List<Long> ids);
}
