package com.example.demo.services.queries;

import com.example.demo.dtos.responses.inventory.InventoryLogListResponse;
import com.example.demo.dtos.responses.inventory.InventoryLogResponse;
import com.example.demo.dtos.responses.inventory.InventoryStatusResponse;

public interface InventoryLogQueryService {

    InventoryLogListResponse getAllInventoryLogs(
            Integer page,
            Integer limit,
            Long warehouseId,
            Long productId,
            String typeStr,
            String fromDate,
            String toDate);

    InventoryLogResponse getInventoryLogById(Long id);

    InventoryStatusResponse getInventoryStatus(Long productId, Long warehouseId);


}
