package com.example.demo.services.commands;

import com.example.demo.dtos.commands.inventory.CreateInventoryLogRequest;
import com.example.demo.dtos.responses.inventory.InventoryLogResponse;

public interface InventoryLogCommandService {

    InventoryLogResponse createInventoryLog(CreateInventoryLogRequest request);

    void deleteInventoryLog(Long id);
}
