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
public class ProductInCartResponse {
    private Long id;

    private String name;

    private String slug;

    private String sku;

    private String image;

    private BigDecimal price;

    @JsonProperty("in_stock")
    private boolean inStock;

    @JsonProperty("max_quantity")
    private int maxQuantity;
}
