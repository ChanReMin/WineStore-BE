package com.example.demo.services.queries;

import com.example.demo.dtos.responses.inventory.InventoryAlertsResponse;
import com.example.demo.dtos.responses.inventory.InventoryDetailResponse;
import com.example.demo.dtos.responses.inventory.InventoryListResponse;

public interface InventoryQueryService {

    InventoryListResponse getAllInventory(
            Integer page,
            Integer limit,
            Long warehouseId,
            Long productId,
            String status,
            String search);

    InventoryDetailResponse getInventoryById(Long inventoryId);

    InventoryAlertsResponse getInventoryAlerts(String type, Long warehouseId);
}
