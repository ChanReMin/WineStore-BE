package com.example.demo.dtos.responses.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {
    @JsonProperty("total_items")
    private int totalItems;

    @JsonProperty("total_quantity")
    private int totalQuantity;

    private BigDecimal subtotal;

    @JsonProperty("estimated_shipping")
    private BigDecimal estimatedShipping;

    @JsonProperty("estimated_total")
    private BigDecimal estimatedTotal;
}
