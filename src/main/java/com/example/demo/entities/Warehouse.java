package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String location;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "warehouse")
    private java.util.List<Inventory> inventories;

    @OneToMany(mappedBy = "warehouse")
    private java.util.List<InventoryLog> inventoryLogs;
}
