package com.example.demo.repositories.queries;

import com.example.demo.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionQueryRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {
}
