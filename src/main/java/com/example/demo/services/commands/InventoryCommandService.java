package com.example.demo.services.commands;

import com.example.demo.dtos.commands.inventory.StockTakeRequest;
import com.example.demo.dtos.commands.inventory.TransferInventoryRequest;
import com.example.demo.dtos.commands.inventory.UpdateInventoryRequest;
import com.example.demo.dtos.responses.inventory.StockTakeResponse;
import com.example.demo.dtos.responses.inventory.TransferInventoryResponse;
import com.example.demo.dtos.responses.inventory.UpdateInventoryResponse;

public interface InventoryCommandService {

    UpdateInventoryResponse updateInventory(Long inventoryId, UpdateInventoryRequest request);

    TransferInventoryResponse transferInventory(TransferInventoryRequest request);

    StockTakeResponse stockTake(StockTakeRequest request);
}
