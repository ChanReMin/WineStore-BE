package com.example.demo.controllers;

import com.example.demo.dtos.commands.warehouse.*;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.warehouse.*;
import com.example.demo.services.commands.AdminWarehouseCommandService;
import com.example.demo.services.commands.WarehouseCommandService;
import com.example.demo.services.queries.WarehouseQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management")
public class WarehouseController {

    private final WarehouseQueryService warehouseQueryService;
    private final WarehouseCommandService warehouseCommandService;
    private final AdminWarehouseCommandService adminWarehouseCommandService;


    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<SuccessResponse<CreateWarehouseResponse>> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {

        CreateWarehouseResponse data = warehouseCommandService.createWarehouse(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<CreateWarehouseResponse>builder()
                        .success(true)
                        .message("Request to create warehouse submitted successfully, pending approval")
                        .data(data)
                        .build());
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<SuccessResponse<Object>> getWarehouses(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Object data = warehouseQueryService.getWarehouses(
                status, managerId, page, limit, search, sortBy, sortOrder
        );

        return ResponseEntity.ok(
                SuccessResponse.builder()
                        .success(true)
                        .data(data)
                        .build()
        );
    }


    /**
     * API 3: GET /seller/warehouses/{id}
     * Xem chi tiết warehouse
     */
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/{warehouseId}")
    public ResponseEntity<SuccessResponse<WarehouseDetailResponse>> getWarehouseById(
            @PathVariable Long warehouseId) {

        WarehouseDetailResponse data = warehouseQueryService.getWarehouseDetailById(warehouseId);

        return ResponseEntity.ok(SuccessResponse.<WarehouseDetailResponse>builder()
                .success(true)
                .data(data)
                .build());
    }

    /**
     * API 4: PUT /seller/warehouses/{id}
     * Cập nhật warehouse (chỉ ACTIVE)
     */
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{warehouseId}")
    public ResponseEntity<SuccessResponse<UpdateWarehouseResponse>> updateWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        UpdateWarehouseResponse data = warehouseCommandService.updateWarehouse(warehouseId, request);

        return ResponseEntity.ok(SuccessResponse.<UpdateWarehouseResponse>builder()
                .success(true)
                .message("Update warehouse request submitted successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<SuccessResponse<Void>> deleteWarehouse(
            @PathVariable Long warehouseId) {

        warehouseCommandService.deleteWarehouse(warehouseId);

        return ResponseEntity.ok(SuccessResponse.<Void>builder()
                .success(true)
                .message("Delete warehouse request submitted successfully")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{warehouseId}/approve")
    public ResponseEntity<SuccessResponse<ApproveWarehouseResponse>> approveWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody(required = false) ApproveWarehouseRequest request) {

        ApproveWarehouseResponse data = adminWarehouseCommandService.approveWarehouse(warehouseId, request);

        return ResponseEntity.ok(SuccessResponse.<ApproveWarehouseResponse>builder()
                .success(true)
                .message("Approved warehouse creation request successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{warehouseId}/reject")
    public ResponseEntity<SuccessResponse<RejectWarehouseResponse>> rejectWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody RejectWarehouseRequest request) {

        RejectWarehouseResponse data = adminWarehouseCommandService.rejectWarehouse(warehouseId, request);

        return ResponseEntity.ok(SuccessResponse.<RejectWarehouseResponse>builder()
                .success(true)
                .message("Rejected warehouse creation request successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{warehouseId}/ban")
    public ResponseEntity<SuccessResponse<BanWarehouseResponse>> banWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody BanWarehouseRequest request) {

        BanWarehouseResponse data = adminWarehouseCommandService.banWarehouse(warehouseId, request);

        return ResponseEntity.ok(SuccessResponse.<BanWarehouseResponse>builder()
                .success(true)
                .message("Locked warehouse successfully")
                .data(data)
                .build());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{warehouseId}/unban")
    public ResponseEntity<SuccessResponse<UnBanWarehouseResponse>> unbanWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody(required = false) UnbanWarehouseRequest request) {

        UnBanWarehouseResponse data = adminWarehouseCommandService.unbanWarehouse(warehouseId, request);

        return ResponseEntity.ok(SuccessResponse.<UnBanWarehouseResponse>builder()
                .success(true)
                .message("Activated warehouse successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/statistics")
    public ResponseEntity<SuccessResponse<WarehouseSellerStatisticsResponse>> getSellerStatistics() {

        WarehouseSellerStatisticsResponse data = warehouseQueryService.getSellerStatistics();

        return ResponseEntity.ok(SuccessResponse.<WarehouseSellerStatisticsResponse>builder()
                .success(true)
                .data(data)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/statistics")
    public ResponseEntity<SuccessResponse<WarehouseAdminStatisticsResponse>> getAdminStatistics(
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        WarehouseAdminStatisticsResponse data = warehouseQueryService.getAdminStatistics(
                managerId, fromDate, toDate
        );

        return ResponseEntity.ok(SuccessResponse.<WarehouseAdminStatisticsResponse>builder()
                .success(true)
                .data(data)
                .build());
    }
}