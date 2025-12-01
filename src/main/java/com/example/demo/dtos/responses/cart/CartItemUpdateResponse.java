package com.example.demo.dtos.responses.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemUpdateResponse {
    @JsonProperty("cart_item_id")
    private Long cartItemId;

    private Integer quantity;

    @JsonProperty("line_total")
    private BigDecimal lineTotal;
}
