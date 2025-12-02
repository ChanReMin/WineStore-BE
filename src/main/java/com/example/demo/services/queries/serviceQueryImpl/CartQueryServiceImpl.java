package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.dtos.responses.cart.*;
import com.example.demo.entities.Cart;
import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repositories.queries.CartItemQueryRepository;
import com.example.demo.repositories.queries.CartQueryRepository;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartQueryServiceImpl {

    private final CartQueryRepository cartQueryRepository;
    private final CartItemQueryRepository cartItemQueryRepository;
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    public CartResponse getUserCart() {
        Long userId = SecurityUtils.getCurrentUserUuid();
        if (userId == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        Optional<Cart> cartOptional = cartQueryRepository.findByUserId(userId);

        if (cartOptional.isEmpty()) {
            // If the user doesn't have a cart, return an empty cart response
            return createEmptyCartResponse(userId);
        }

        Cart cart = cartOptional.get();
        List<CartItem> cartItems = cartItemQueryRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            return createEmptyCartResponse(userId, cart.getId());
        }

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        CartSummaryResponse summary = calculateSummary(itemResponses);

        return new CartResponse(
                cart.getId(),
                userId,
                itemResponses,
                summary,
                cart.getUpdatedAt()
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        String imageUrl = product.getImages();
        // Attempt to parse if it looks like a JSON array
        if (imageUrl != null && imageUrl.trim().startsWith("[") && imageUrl.trim().endsWith("]")) {
            try {
                List<String> imagesList = objectMapper.readValue(imageUrl, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                if (!imagesList.isEmpty()) {
                    imageUrl = imagesList.get(0); // Take the first image
                } else {
                    imageUrl = null; // No images in array
                }
            } catch (JsonProcessingException e) {
                // Not a valid JSON array, treat as a single URL string or null
                // Log error or keep original imageUrl
                // In a real application, you might want to log this error or use a default image.
                imageUrl = product.getImages(); // Keep original if parsing fails
            }
        }


        ProductInCartResponse productResponse = new ProductInCartResponse(
                product.getId(),
                product.getName(),
                product.getSlug() != null ? product.getSlug() : "",
                product.getSku() != null ? product.getSku() : "",
                imageUrl != null ? imageUrl : "",
                product.getPrice(),
                product.isInStock(),
                product.getTotalInventory()
        );

        BigDecimal unitPrice = product.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponse(
                cartItem.getId(),
                productResponse,
                cartItem.getQuantity(),
                unitPrice,
                lineTotal,
                cartItem.getCreatedAt() // Assuming createdAt as addedAt
        );
    }

    private CartSummaryResponse calculateSummary(List<CartItemResponse> items) {
        int totalItems = items.size();
        int totalQuantity = items.stream().mapToInt(CartItemResponse::getQuantity).sum();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartSummaryResponse(
                totalItems,
                totalQuantity,
                subtotal,
                BigDecimal.ZERO, // Estimated shipping, for now 0
                subtotal // Estimated total, for now same as subtotal
        );
    }

    private CartResponse createEmptyCartResponse(Long userId) {
        return createEmptyCartResponse(userId, null);
    }

    private CartResponse createEmptyCartResponse(Long userId, Long cartId) {
        CartSummaryResponse summary = new CartSummaryResponse(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return new CartResponse(
                cartId,
                userId,
                Collections.emptyList(),
                summary,
                LocalDateTime.now()
        );
    }
}
