package com.example.demo.repositories.queries;
import com.example.demo.dtos.responses.brand.BrandListItemResponse;
import com.example.demo.entities.Brand;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandQueryRepository extends JpaRepository<Brand, Long> {
    @Query("""
       SELECT new com.example.demo.dtos.responses.brand.BrandListItemResponse(
            b.id,
            b.name,
            b.country,
            COUNT(p.id)
       )
       FROM Brand b
       LEFT JOIN Product p ON p.brand = b
       GROUP BY b.id, b.name, b.country
       ORDER BY b.id
       """)
    List<BrandListItemResponse> getAllBrandWithProductCount();
    @EntityGraph(attributePaths = {"products"})
    @Query("SELECT b FROM Brand b WHERE b.deletedAt IS NULL ORDER BY b.name ASC")
    List<Brand> findAllWithProductCount();

}

