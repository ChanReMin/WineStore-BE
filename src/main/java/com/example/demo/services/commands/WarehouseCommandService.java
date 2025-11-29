package com.example.demo.services.commands;

import com.example.demo.dtos.commands.warehouse.CreateWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.UpdateWarehouseRequest;
import com.example.demo.dtos.responses.warehouse.CreateWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.UpdateWarehouseResponse;

public interface WarehouseCommandService {

    CreateWarehouseResponse createWarehouse(CreateWarehouseRequest request);

    UpdateWarehouseResponse updateWarehouse(Long warehouseId, UpdateWarehouseRequest request);

    void deleteWarehouse(Long warehouseId);
}
