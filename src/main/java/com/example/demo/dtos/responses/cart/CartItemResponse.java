package com.example.demo.dtos.responses.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;

    private ProductInCartResponse product;

    private int quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("line_total")
    private BigDecimal lineTotal;

    @JsonProperty("added_at")
    private LocalDateTime addedAt;
}
