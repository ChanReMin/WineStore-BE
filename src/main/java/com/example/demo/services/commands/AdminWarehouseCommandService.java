package com.example.demo.services.commands;

import com.example.demo.dtos.commands.warehouse.ApproveWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.BanWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.RejectWarehouseRequest;
import com.example.demo.dtos.commands.warehouse.UnbanWarehouseRequest;
import com.example.demo.dtos.responses.warehouse.ApproveWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.BanWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.RejectWarehouseResponse;
import com.example.demo.dtos.responses.warehouse.UnBanWarehouseResponse;

public interface AdminWarehouseCommandService {
    ApproveWarehouseResponse approveWarehouse(Long warehouseId, ApproveWarehouseRequest request);

    RejectWarehouseResponse rejectWarehouse(Long warehouseId, RejectWarehouseRequest request);

    BanWarehouseResponse banWarehouse(Long warehouseId, BanWarehouseRequest request);

    UnBanWarehouseResponse unbanWarehouse(Long warehouseId, UnbanWarehouseRequest request);

}
