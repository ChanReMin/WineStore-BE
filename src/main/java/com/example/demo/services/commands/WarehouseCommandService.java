package com.example.demo.services.commands;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.configs.SecurityUtils;
import com.example.demo.dtos.commands.warehouse.CreateWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.UpdateWarehouseRequest;
import com.example.demo.dtos.mappers.warehouse.WarehouseMapper;
import com.example.demo.dtos.responses.warehouse.CreateWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.UpdateWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Warehouse;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.WarehouseCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseCommandService {

    private final WarehouseCommandRepository warehouseCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final SecurityUtils securityUtils;
    private final WarehouseMapper warehouseMapper;

    /**
     * API 1: POST /seller/warehouses
     * Create warehouse request (status = PENDING)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public CreateWarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        log.info("🏭 [SELLER] Creating warehouse request: {}", request.getName());

        Account currentAccount = getCurrentAccount();

        // Check duplicate
        if (warehouseCommandRepository.existsByNameAndManagerIdAndDeletedAtIsNull(
                request.getName(), currentAccount.getId())) {
            throw new DuplicateResourceException(
                    "Kho với tên '" + request.getName() + "' đã tồn tại",
                    "name"
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .status(ProductStatus.PENDING)
                .createdBy(currentAccount)
                .build();

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse request created with id: {}", saved.getId());

        return CreateWarehouseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .description(saved.getDescription())
                .status(saved.getStatus().getCode())
                .managerId(saved.getCreatedBy().getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }


    /**
     * API 4: PUT /seller/warehouses/{id}
     * Update warehouse (only ACTIVE warehouses)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateWarehouseResponse updateWarehouse(Long warehouseId, UpdateWarehouseRequest request) {
        log.info("✏️ [SELLER] Updating warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng"));

        validateOwnership(warehouse);

        // Chỉ cho phép cập nhật warehouse ACTIVE
        if (!warehouse.isActive()) {
            if (warehouse.isPending()) {
                throw new ForbiddenException("Không thể cập nhật kho đang chờ duyệt");
            }
            if (warehouse.isBanned()) {
                throw new ForbiddenException("Không thể cập nhật kho đã bị khóa");
            }
        }

        // Update fields
        if (request.getName() != null) {
            if (warehouseCommandRepository.existsByNameAndManagerIdAndIdNotAndDeletedAtIsNull(
                    request.getName(), warehouse.getCreatedBy().getId(), warehouseId)) {
                throw new DuplicateResourceException(
                        "Kho với tên '" + request.getName() + "' đã tồn tại",
                        "name"
                );
            }
            warehouse.setName(request.getName());
        }

        if (request.getLocation() != null) {
            warehouse.setLocation(request.getLocation());
        }

        if (request.getDescription() != null) {
            warehouse.setDescription(request.getDescription());
        }

        warehouse.setUpdatedAt(LocalDateTime.now());

        Warehouse updated = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse updated successfully");

        return UpdateWarehouseResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .location(updated.getLocation())
                .description(updated.getDescription())
                .status(updated.getStatus().getCode())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }


    /**
     * API 5: DELETE /seller/warehouses/{id}
     * Delete warehouse request (only PENDING)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteWarehouse(Long warehouseId) {
        log.info("🗑️ [SELLER] Deleting warehouse request: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kho hàng"));

        validateOwnership(warehouse);

        if (!warehouse.isPending()) {
            throw new ForbiddenException("Chỉ có thể xóa kho đang chờ duyệt");
        }

        warehouse.softDelete();
        warehouseCommandRepository.save(warehouse);

        log.info("✅ Warehouse request deleted successfully");
    }

    // Helper methods
    private Account getCurrentAccount() {
        return accountCommandRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateOwnership(Warehouse warehouse) {
        if (warehouse.getCreatedBy() == null ||
                !securityUtils.isOwner(warehouse.getCreatedBy().getEmail())) {
            throw new ForbiddenException("Bạn không có quyền truy cập kho này");
        }
    }

}
