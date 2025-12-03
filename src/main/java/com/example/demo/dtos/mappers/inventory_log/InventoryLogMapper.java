package com.example.demo.dtos.mappers.inventory_log;

import com.example.demo.commons.enums.InventoryLogType;

import com.example.demo.dtos.commands.inventory.CreateInventoryLogRequest;
import com.example.demo.dtos.responses.inventory.InventoryLogDetailResponse;
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
                .createdBy(log.getUser() != null
                        ? log.getUser().getFirstName() + " " + log.getUser().getLastName()
                        : "System")
                .createdAt(log.getCreatedAt())
                .build();
    }

    public InventoryLogDetailResponse toDetailResponse(
            InventoryLog log,
            Integer quantityBefore,
            Integer quantityAfter) {

        // Calculate quantity change
        Integer change = calculateQuantityChange(log.getType(), log.getQuantity());

        // Build warehouse info with manager
        InventoryLogDetailResponse.WarehouseInfo.ManagerInfo manager = null;
        if (log.getWarehouse().getCreatedBy() != null &&
                log.getWarehouse().getCreatedBy().getUser() != null) {
            manager = InventoryLogDetailResponse.WarehouseInfo.ManagerInfo.builder()
                    .name(log.getWarehouse().getCreatedBy().getUser().getFirstName() + " " +
                            log.getWarehouse().getCreatedBy().getUser().getLastName())
                    .phone(log.getWarehouse().getCreatedBy().getUser().getPhoneNumber())
                    .build();
        }

        InventoryLogDetailResponse.WarehouseInfo warehouse =
                InventoryLogDetailResponse.WarehouseInfo.builder()
                        .id(log.getWarehouse().getId())
                        .name(log.getWarehouse().getName())
                        .location(log.getWarehouse().getLocation())
                        .city(log.getWarehouse().getCity())
                        .manager(manager)
                        .build();

        // Build product info
        InventoryLogDetailResponse.ProductInfo product =
                InventoryLogDetailResponse.ProductInfo.builder()
                        .id(log.getProduct().getId())
                        .name(log.getProduct().getName())
                        .sku(log.getProduct().getSku())
                        .price(log.getProduct().getPrice())
                        .costPrice(log.getProduct().getCostPrice())
                        .image(log.getProduct().getImages())
                        .build();

        // Build quantity change info
        InventoryLogDetailResponse.QuantityChange quantityChange =
                InventoryLogDetailResponse.QuantityChange.builder()
                        .before(quantityBefore)
                        .change(change)
                        .after(quantityAfter)
                        .build();

        // Build user info
        InventoryLogDetailResponse.UserInfo user = null;
        if (log.getUser() != null) {
            user = InventoryLogDetailResponse.UserInfo.builder()
                    .id(log.getUser().getId())
                    .name(log.getUser().getFirstName() + " " + log.getUser().getLastName())
                    .email(log.getUser().getAccount() != null
                            ? log.getUser().getAccount().getEmail()
                            : null)
                    .role(log.getUser().getAccount() != null &&
                            log.getUser().getAccount().getRole() != null
                            ? log.getUser().getAccount().getRole().name()
                            : "UNKNOWN")
                    .build();
        }

        return InventoryLogDetailResponse.builder()
                .id(log.getId())
                .type(log.getType().getDescription())
                .typeText(log.getType().getDescription())
                .quantity(log.getQuantity())
                .createdAt(log.getCreatedAt())
                .warehouse(warehouse)
                .product(product)
                .quantityChange(quantityChange)
                .user(user)
                .note(log.getNote())
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
}