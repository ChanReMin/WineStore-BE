package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.WarehouseStatus;
import com.example.demo.services.commands.AdminWarehouseCommandService;
import com.example.demo.utils.SecurityUtils;
import com.example.demo.dtos.commands.warehouse.*;
import com.example.demo.dtos.responses.warehouse.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.Warehouse;
import com.example.demo.exceptions.BadRequestException;
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
public class AdminWarehouseCommandServiceImpl implements AdminWarehouseCommandService {

    private final WarehouseCommandRepository warehouseCommandRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final SecurityUtils securityUtils;

    @Transactional(transactionManager = "writeTransactionManager")
    public ApproveWarehouseResponse approveWarehouse(Long warehouseId, ApproveWarehouseRequest request) {
        log.info("✅ [ADMIN] Approving warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse request not found"));

        if (warehouse.isApproved()) {
            throw new BadRequestException("Warehouse already approved");
        }

        Account admin = getCurrentAccount();

        warehouse.setStatus(WarehouseStatus.APPROVE);
        warehouse.setApprovedBy(admin);
        warehouse.setApprovedAt(LocalDateTime.now());
        if (request != null && request.getNote() != null) {
            warehouse.setApprovalNote(request.getNote());
        }

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse approved successfully");

        return ApproveWarehouseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .status(saved.getStatus().getCode())
                .managerId(saved.getCreatedBy() != null ? saved.getCreatedBy().getId() : null)
                .updateAt(saved.getUpdatedAt())
                .build();
    }


    @Transactional(transactionManager = "writeTransactionManager")
    public RejectWarehouseResponse rejectWarehouse(Long warehouseId, RejectWarehouseRequest request) {
        log.info("❌ [ADMIN] Rejecting warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse request not found"));

        warehouse.setStatus(WarehouseStatus.REJECT);
        warehouse.setRejectionReason(request.getReason());

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse request rejected and deleted");

        return RejectWarehouseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .status(saved.getStatus().getCode())
                .rejectionNote(saved.getRejectionReason())
                .build();
    }


    @Transactional(transactionManager = "writeTransactionManager")
    public BanWarehouseResponse banWarehouse(Long warehouseId, BanWarehouseRequest request) {
        log.info("🚫 [ADMIN] Banning warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (warehouse.isBanned()) {
            throw new BadRequestException("Warehouse is already banned");
        }

        warehouse.setStatus(WarehouseStatus.BAN);
        warehouse.setBanReason(request.getReason());
        warehouse.setUpdatedAt(LocalDateTime.now());

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse banned successfully");

        return BanWarehouseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .status(saved.getStatus().getCode())
                .managerId(saved.getCreatedBy() != null ? saved.getCreatedBy().getId() : null)
                .updatedAt(saved.getUpdatedAt())
                .banNote(saved.getBanReason())
                .build();
    }


    @Transactional(transactionManager = "writeTransactionManager")
    public UnBanWarehouseResponse unbanWarehouse(Long warehouseId, UnbanWarehouseRequest request) {
        log.info("🔓 [ADMIN] Unbanning warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseCommandRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        if (!warehouse.isBanned()) {
            throw new BadRequestException("Warehouse is not banned");
        }

        warehouse.setStatus(WarehouseStatus.APPROVE);
        warehouse.setBanReason(null); // Clear ban reason

        if (request != null && request.getNote() != null) {
            warehouse.setApprovalNote(request.getNote());
        }

        Warehouse saved = warehouseCommandRepository.save(warehouse);
        log.info("✅ Warehouse unbanned successfully");

        return UnBanWarehouseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .status(saved.getStatus().getCode())
                .managerId(saved.getCreatedBy() != null ? saved.getCreatedBy().getId() : null)
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    private Account getCurrentAccount() {
        return accountCommandRepository.findByEmail(securityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }
}