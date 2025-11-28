package com.example.demo.repositories.queries;

import com.example.demo.dtos.responses.category.CategoryListItemResponse;
import com.example.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryQueryRepository extends JpaRepository<Category, Long> {
    @Query("""
       SELECT new com.example.demo.dtos.responses.category.CategoryListItemResponse(
            c.id,
            c.name,
            c.slug,
            c.description,
            COUNT(p.id)
       )
       FROM Category c
       LEFT JOIN Product p ON p.category = c
       GROUP BY c.id, c.name, c.slug, c.description
       ORDER BY c.id
       """)
    List<CategoryListItemResponse> getAllCategoryWithProductCount();
}
