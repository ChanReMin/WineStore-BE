package com.example.demo.repositories.commands;

import com.example.demo.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseCommandRepository extends JpaRepository<Warehouse, Long> {

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
            "FROM Warehouse w WHERE w.name = :name AND w.createdBy.id = :managerId AND w.deletedAt IS NULL")
    boolean existsByNameAndManagerIdAndDeletedAtIsNull(@Param("name") String name, @Param("managerId") Long managerId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
            "FROM Warehouse w WHERE w.name = :name AND w.createdBy.id = :managerId " +
            "AND w.id != :id AND w.deletedAt IS NULL")
    boolean existsByNameAndManagerIdAndIdNotAndDeletedAtIsNull(
            @Param("name") String name,
            @Param("managerId") Long managerId,
            @Param("id") Long id);
}