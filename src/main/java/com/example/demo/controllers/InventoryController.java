package com.example.demo.controllers;

import com.example.demo.dtos.commands.inventory.CreateInventoryLogRequest;
import com.example.demo.dtos.commands.inventory.StockTakeRequest;
import com.example.demo.dtos.commands.inventory.TransferInventoryRequest;
import com.example.demo.dtos.commands.inventory.UpdateInventoryRequest;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.inventory.*;
import com.example.demo.services.commands.InventoryCommandService;
import com.example.demo.services.commands.InventoryLogCommandService;
import com.example.demo.services.queries.InventoryLogQueryService;
import com.example.demo.services.queries.InventoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management")
public class InventoryController {

    private final InventoryLogCommandService inventoryLogCommandService;
    private final InventoryLogQueryService inventoryLogQueryService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryCommandService inventoryCommandService;

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<InventoryListResponse>> getAllInventory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(name = "warehouseId", required = false) Long warehouseId,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        InventoryListResponse data = inventoryQueryService.getAllInventory(
                page, limit, warehouseId, productId, status, search);

        return ResponseEntity.ok(SuccessResponse.<InventoryListResponse>builder()
                .success(true)
                .data(data)
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @GetMapping("/{inventoryId}")
    public ResponseEntity<SuccessResponse<InventoryDetailResponse>> getInventoryById(
            @PathVariable Long inventoryId) {

        InventoryDetailResponse data = inventoryQueryService.getInventoryById(inventoryId);

        return ResponseEntity.ok(SuccessResponse.<InventoryDetailResponse>builder()
                .success(true)
                .data(data)
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @PutMapping("/{inventoryId}")
    public ResponseEntity<SuccessResponse<UpdateInventoryResponse>> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateInventoryRequest request) {

        UpdateInventoryResponse data = inventoryCommandService.updateInventory(inventoryId, request);

        return ResponseEntity.ok(SuccessResponse.<UpdateInventoryResponse>builder()
                .success(true)
                .message("Update inventory successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @PostMapping("/transfer")
    public ResponseEntity<SuccessResponse<TransferInventoryResponse>> transferInventory(
            @Valid @RequestBody TransferInventoryRequest request) {

        TransferInventoryResponse data = inventoryCommandService.transferInventory(request);

        return ResponseEntity.ok(SuccessResponse.<TransferInventoryResponse>builder()
                .success(true)
                .message("Created inventory transfer successfully")
                .data(data)
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @PostMapping("/stock-take")
    public ResponseEntity<SuccessResponse<StockTakeResponse>> stockTake(
            @Valid @RequestBody StockTakeRequest request) {

        StockTakeResponse data = inventoryCommandService.stockTake(request);

        return ResponseEntity.ok(SuccessResponse.<StockTakeResponse>builder()
                .success(true)
                .message("Stock take completed successfully")
                .data(data)
                .build());
    }


    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/alerts")
    public ResponseEntity<SuccessResponse<InventoryAlertsResponse>> getInventoryAlerts(
            @RequestParam(required = false) String type,
            @RequestParam(name = "warehouse_id", required = false) Long warehouseId) {

        InventoryAlertsResponse data = inventoryQueryService.getInventoryAlerts(type, warehouseId);

        return ResponseEntity.ok(SuccessResponse.<InventoryAlertsResponse>builder()
                .success(true)
                .data(data)
                .build());
    }


    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/logs")
    public ResponseEntity<SuccessResponse<InventoryLogListResponse>> getAllInventoryLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(name = "warehouseId", required = false) Long warehouseId,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(required = false) String type,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate) {

        InventoryLogListResponse data = inventoryLogQueryService.getAllInventoryLogs(
                page, limit, warehouseId, productId, type, fromDate, toDate);

        return ResponseEntity.ok(SuccessResponse.<InventoryLogListResponse>builder()
                .success(true)
                .data(data)
                .build());
    }


    @PreAuthorize("hasAnyRole('SELLER')")
    @GetMapping("/status")
    public ResponseEntity<SuccessResponse<InventoryStatusResponse>> getInventoryStatus(
            @RequestParam(name = "product_id") Long productId,
            @RequestParam(name = "warehouse_id") Long warehouseId) {

        InventoryStatusResponse data = inventoryLogQueryService.getInventoryStatus(
                productId, warehouseId);

        return ResponseEntity.ok(SuccessResponse.<InventoryStatusResponse>builder()
                .success(true)
                .data(data)
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER')")
    @PostMapping
    public ResponseEntity<SuccessResponse<InventoryLogResponse>> createInventoryLog(
            @Valid @RequestBody CreateInventoryLogRequest request) {

        InventoryLogResponse data = inventoryLogCommandService.createInventoryLog(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<InventoryLogResponse>builder()
                        .success(true)
                        .message("Update inventory log created successfully")
                        .data(data)
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteInventoryLog(
            @PathVariable Long id) {

        inventoryLogCommandService.deleteInventoryLog(id);

        return ResponseEntity.ok(SuccessResponse.<Void>builder()
                .success(true)
                .message("Inventory log deleted successfully")
                .build());
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @GetMapping("/logs/{logId}")
    @Operation(summary = "Get inventory log detail by ID")
    public ResponseEntity<SuccessResponse<InventoryLogDetailResponse>> getInventoryLogDetail(
            @PathVariable Long logId) {

        InventoryLogDetailResponse data = inventoryLogQueryService.getInventoryLogDetailById(logId);

        return ResponseEntity.ok(SuccessResponse.<InventoryLogDetailResponse>builder()
                .success(true)
                .data(data)
                .build());
    }
}
