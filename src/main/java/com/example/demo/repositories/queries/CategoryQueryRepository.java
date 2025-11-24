package com.example.demo.repositories.queries;

import com.example.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryQueryRepository extends JpaRepository<Category, Long> {
}
