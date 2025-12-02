package com.example.demo.dtos.commands.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddItemToCartRequest {
    @NotNull(message = "Product ID cannot be null")
    @JsonProperty("product_id")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;
}
