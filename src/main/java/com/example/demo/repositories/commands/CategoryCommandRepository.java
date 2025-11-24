package com.example.demo.repositories.commands;

import com.example.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryCommandRepository extends JpaRepository<Category, Long> {
}
