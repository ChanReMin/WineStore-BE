package com.example.demo.entities;

import com.example.demo.commons.enums.WarehouseStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_manager_id", columnList = "manager_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 512)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String city;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    private WarehouseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Account createdBy;

    // Admin actions
    @Column(name = "approval_note", length = 1000)
    private String approvalNote;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "ban_reason", length = 1000)
    private String banReason;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private java.util.List<Inventory> inventories;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private java.util.List<InventoryLog> inventoryLogs;

    // Helper methods
    public boolean isPending() {
        return status == WarehouseStatus.PENDING;
    }
    public boolean isReject() {
        return status == WarehouseStatus.REJECT;
    }

    public boolean isBanned() {
        return status == WarehouseStatus.BAN;
    }
    public boolean isApproved() {return status == WarehouseStatus.APPROVE;}
}