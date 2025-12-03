package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.NotificationStatus;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.commons.enums.WarehouseStatus;
import com.example.demo.dtos.notifications.NotificationMessage;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.services.commands.WarehouseCommandService;
import com.example.demo.services.notifications.NotificationProducer;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.commands.warehouse.CreateWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.UpdateWarehouseRequest;
import com.example.demo.dtos.mappers.warehouse.WarehouseMapper;
import com.example.demo.dtos.responses.warehouse.CreateWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.UpdateWarehouseResponse;
import com.example.demo.entities.Account;
import com.example.demo.entities.Warehouse;
import com.example.demo.exceptions.DuplicateResourceException;
import com.example.demo.exceptions.ForbiddenException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.WarehouseCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseCommandServiceImpl implements WarehouseCommandService {

    private final WarehouseCommandRepository warehouseCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final SecurityUtils securityUtils;
    private final WarehouseMapper warehouseMapper;
    private final NotificationProducer notificationProducer;
    private final AccountQueryRepository accountQueryRepository;

    @Value("${application.fe-endpoint}")
    public String feEndpoint;

    /**
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
                    "Warehouse name '" + request.getName() + "' is already taken",
                    "name"
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .city(request.getCity())
                .status(WarehouseStatus.PENDING)
                .createdBy(currentAccount)
                .build();

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse request created with id: {}", saved.getId());

        Long adminId = accountQueryRepository.findFirstAdminId()
                .orElseThrow(() -> new IllegalStateException("No admin account found"));

        NotificationMessage msg = NotificationMessage.builder()
                .id(0L)
                .userId(adminId)
                .title("WAREHOUSE REQUIRED APPROVAL")
                .message("A warehouse has been created and required for approval")
                .status(NotificationStatus.SUCCESS)
                .itemUrl(feEndpoint + "/admin/warehouse-approval")
                .createdAt(Instant.now())
                .build();

        notificationProducer.send(msg);

        Long userId = SecurityUtils.getCurrentUserUuid();
        NotificationMessage sellerMsg = NotificationMessage.builder()
                .id(0L)
                .userId(userId) //SEND NOTIFICATION TO THIS USER
                .title("WAREHOUSE CREATED SUCCESS")
                .message("Your warehouse has been created and been pended for admin approval.")
                .status(NotificationStatus.SUCCESS)
                .itemUrl(feEndpoint + "/shop/" + saved.getId() + "/" + saved.getName())
                .createdAt(Instant.now())
                .build();
        notificationProducer.send(sellerMsg);

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
     * Update warehouse (only ACTIVE warehouses)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateWarehouseResponse updateWarehouse(Long warehouseId, UpdateWarehouseRequest request) {
        log.info("✏️ [SELLER] Updating warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found warehouse"));

        validateOwnership(warehouse);

        // Only allow ACTIVE warehouse updates
        if (!warehouse.isApproved()) {
            if (warehouse.isPending()) {
                throw new ForbiddenException("Not allowed to update pending warehouse");
            }
            if (warehouse.isBanned()) {
                throw new ForbiddenException("Not allowed to update banned warehouse");
            }
        }

        // Update fields
        if (request.getName() != null) {
            if (warehouseCommandRepository.existsByNameAndManagerIdAndIdNotAndDeletedAtIsNull(
                    request.getName(), warehouse.getCreatedBy().getId(), warehouseId)) {
                throw new DuplicateResourceException(
                        "Warehouse name: '" + request.getName() + "' is already taken",
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

        Long adminId = accountQueryRepository.findFirstAdminId()
                .orElseThrow(() -> new IllegalStateException("No admin account found"));
        NotificationMessage msg = NotificationMessage.builder()
                .id(0L)
                .userId(adminId)
                .title("WAREHOUSE REQUIRED APPROVAL")
                .message("A warehouse has been created and required for approval")
                .status(NotificationStatus.SUCCESS)
                .itemUrl(feEndpoint + "/admin/warehouse-approval")
                .createdAt(Instant.now())
                .build();

        notificationProducer.send(msg);

        Long userId = SecurityUtils.getCurrentUserUuid();
        NotificationMessage sellerMsg = NotificationMessage.builder()
                .id(0L)
                .userId(userId) //SEND NOTIFICATION TO THIS USER
                .title("WAREHOUSE UPDATED SUCCESS")
                .message("Your warehouse has been updated and been pended for admin approval.")
                .status(NotificationStatus.SUCCESS)
                .itemUrl(feEndpoint + "/shop/" + updated.getId() + "/" + updated.getName())
                .createdAt(Instant.now())
                .build();
        notificationProducer.send(sellerMsg);

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
     * Delete warehouse request (only PENDING)
     */
    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteWarehouse(Long warehouseId) {
        log.info("🗑️ [SELLER] Deleting warehouse request: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found warehouse"));

        validateOwnership(warehouse);

        if (!warehouse.isPending()) {
            throw new ForbiddenException("Only pending warehouses can be deleted");
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
            throw new ForbiddenException("User is not the owner of the warehouse");
        }
    }

}
