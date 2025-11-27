package com.example.demo.repositories.commands;

import com.example.demo.entities.Inventory;
import com.example.demo.entities.Product;
import com.example.demo.entities.Warehouse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryCommandRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product = :product AND i.warehouse = :warehouse AND i.deletedAt IS NULL")
    Optional<Inventory> findByProductAndWarehouseWithLock(
            @Param("product") Product product,
            @Param("warehouse") Warehouse warehouse
    );

    @Query("SELECT i FROM Inventory i WHERE i.product = :product AND i.warehouse = :warehouse AND i.deletedAt IS NULL")
    Optional<Inventory> findByProductAndWarehouse(
            @Param("product") Product product,
            @Param("warehouse") Warehouse warehouse
    );
}