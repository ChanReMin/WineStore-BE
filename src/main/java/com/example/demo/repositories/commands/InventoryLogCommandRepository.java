package com.example.demo.repositories.commands;

import com.example.demo.entities.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogCommandRepository extends JpaRepository<InventoryLog, Long> {
}
