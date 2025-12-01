package com.example.demo.controllers;

import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.cart.CartResponse;
import com.example.demo.services.queries.CartQueryService;
import com.example.demo.dtos.commands.cart.AddItemToCartRequest;
import com.example.demo.dtos.commands.cart.UpdateCartItemQuantityRequest; // New import
import com.example.demo.dtos.responses.cart.CartItemAddResponse;
import com.example.demo.dtos.responses.cart.CartItemUpdateResponse; // New import
import com.example.demo.services.commands.CartCommandService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartQueryService cartQueryService;
    private final CartCommandService cartCommandService;

    @GetMapping
    public ResponseEntity<SuccessResponse<CartResponse>> getCart() {
        CartResponse cartResponse = cartQueryService.getUserCart();
        return ResponseEntity.ok(SuccessResponse.<CartResponse>builder()
                .success(true)
                .message("Cart retrieved successfully")
                .data(cartResponse)
                .build());
    }

    @PostMapping("/items")
    public ResponseEntity<SuccessResponse<CartItemAddResponse>> addItemToCart(
            @Valid @RequestBody AddItemToCartRequest request) {
        CartItemAddResponse response = cartCommandService.addItemToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.<CartItemAddResponse>builder()
                .success(true)
                .message("Product added to cart")
                .data(response)
                .build());
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<SuccessResponse<CartItemUpdateResponse>> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        CartItemUpdateResponse response = cartCommandService.updateCartItemQuantity(cartItemId, request);
        return ResponseEntity.ok(SuccessResponse.<CartItemUpdateResponse>builder()
                .success(true)
                .message("Quantity updated successfully") // Message from prompt
                .data(response)
                .build());
    }
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<SuccessResponse<Void>> deleteCartItem(@PathVariable Long cartItemId) {
        cartCommandService.deleteCartItem(cartItemId);
        return ResponseEntity.ok(SuccessResponse.<Void>builder()
                .success(true)
                .message("Product removed from cart") // Message from prompt
                .build());
    }
    @DeleteMapping
    public ResponseEntity<SuccessResponse<Void>> clearCart() {
        cartCommandService.clearCart();
        return ResponseEntity.ok(SuccessResponse.<Void>builder()
                .success(true)
                .message("Cart cleared successfully") // Message from prompt
                .build());
    }
}
