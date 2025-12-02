package com.example.demo.dtos.responses.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    @JsonProperty("cart_id")
    private Long cartId;

    @JsonProperty("user_id")
    private Long userId;

    private List<CartItemResponse> items;

    private CartSummaryResponse summary;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
