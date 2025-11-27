package com.example.demo.dtos.mappers.inventory_log;

import com.example.demo.commons.enums.InventoryLogType;

import com.example.demo.dtos.commands.inventory.CreateInventoryLogRequest;
import com.example.demo.dtos.responses.inventory.InventoryLogResponse;
import com.example.demo.entities.InventoryLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryLogMapper {

    /**
     * Map CreateRequest to Entity
     * Note: warehouse, product, user will be set in service layer
     */
    public InventoryLog toEntity(CreateInventoryLogRequest request) {
        return InventoryLog.builder()
                .type(request.getTypeEnum())
                .quantity(request.getQuantity())
                .shipmentId(request.getShipmentId())
                .note(request.getNote())
                .build();
    }

    /**
     * Map Entity to Response
     * Includes quantity_before, quantity_change, quantity_after calculation
     */
    public InventoryLogResponse toResponse(InventoryLog log) {
        return toResponse(log, null, null, null);
    }

    /**
     * Map Entity to Response with inventory state info
     */
    public InventoryLogResponse toResponse(
            InventoryLog log,
            Integer quantityBefore,
            Integer quantityChange,
            Integer quantityAfter) {

        // Calculate quantity change based on type
        int calculatedChange = calculateQuantityChange(log.getType(), log.getQuantity());

        return InventoryLogResponse.builder()
                .id(log.getId())
                .type(log.getType().name().toLowerCase())
                .typeText(log.getType().getDescription())
                .warehouse(InventoryLogResponse.WarehouseInfo.builder()
                        .id(log.getWarehouse().getId())
                        .name(log.getWarehouse().getName())
                        .location(log.getWarehouse().getLocation())
                        .address(log.getWarehouse().getCity())
                        .build())
                .product(InventoryLogResponse.ProductInfo.builder()
                        .id(log.getProduct().getId())
                        .name(log.getProduct().getName())
                        .sku(log.getProduct().getSku())
                        .price(log.getProduct().getPrice())
                        .image(log.getProduct().getImages())
                        .build())
                .quantityBefore(quantityBefore)
                .quantityChange(quantityChange != null ? quantityChange : calculatedChange)
                .quantityAfter(quantityAfter)
                .note(log.getNote())
                .referenceCode(generateReferenceCode(log))
                .createdBy(log.getUser() != null
                        ? log.getUser().getFirstName() + " " + log.getUser().getLastName()
                        : "System")
                .createdAt(log.getCreatedAt())
                .build();
    }

    /**
     * Calculate quantity change based on operation type
     * Positive for IN/RETURN, Negative for OUT, Variable for ADJUST
     */
    private int calculateQuantityChange(InventoryLogType type, Integer quantity) {
        switch (type) {
            case IN:
            case RETURN:
            case TRANSFER_IN:
                return quantity;
            case OUT:
            case TRANSFER_OUT:
                return -quantity;
            case ADJUST:
                // For ADJUST, quantity can be positive or negative
                return quantity;
            default:
                return 0;
        }
    }

    /**
     * Generate reference code based on log type and shipment ID
     */
    private String generateReferenceCode(InventoryLog log) {
        if (log.getShipmentId() != null) {
            switch (log.getType()) {
                case IN:
                    return "PO-" + log.getShipmentId();
                case OUT:
                    return "ORD-" + log.getShipmentId();
                case TRANSFER_OUT:
                case TRANSFER_IN:
                    return "TRF-" + log.getShipmentId();
                default:
                    return null;
            }
        }
        return null;
    }
}