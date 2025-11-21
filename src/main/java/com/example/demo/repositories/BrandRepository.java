package com.example.demo.repositories;

import com.example.demo.dtos.responses.brand.BrandResponse;
import com.example.demo.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("SELECT new com.example.demo.dtos.responses.brand.BrandResponse(b.id, b.name, b.country, b.description) FROM Brand b")
    List<BrandResponse> findAllBrandsWithRequiredFields();
}
