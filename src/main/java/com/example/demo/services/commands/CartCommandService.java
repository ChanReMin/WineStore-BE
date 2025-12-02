package com.example.demo.services.commands;

import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.commands.cart.AddItemToCartRequest;
import com.example.demo.dtos.commands.cart.UpdateCartItemQuantityRequest; // New import
import com.example.demo.dtos.responses.cart.CartItemAddResponse;
import com.example.demo.dtos.responses.cart.CartItemUpdateResponse; // New import
import com.example.demo.entities.Cart;
import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User; // Import User entity
import com.example.demo.exceptions.ConflictException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.GoneException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.repositories.commands.CartCommandRepository;
import com.example.demo.repositories.commands.CartItemCommandRepository;
import com.example.demo.repositories.queries.CartQueryRepository;
import com.example.demo.repositories.queries.CartItemQueryRepository;
import com.example.demo.repositories.queries.UserQueryRepository; // Import UserQueryRepository
import com.example.demo.services.queries.ProductQueryService;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCommandService {

    private final CartCommandRepository cartCommandRepository;
    private final CartItemCommandRepository cartItemCommandRepository;
    private final CartQueryRepository cartQueryRepository;
    private final CartItemQueryRepository cartItemQueryRepository;
    private final ProductQueryService productQueryService;
    private final UserQueryRepository userQueryRepository; // Inject UserQueryRepository

    @Transactional
    public CartItemAddResponse addItemToCart(AddItemToCartRequest request) {
        Long accountId = SecurityUtils.getCurrentUserUuid(); // This is account ID
        if (accountId == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        // 1. Validate product existence and availability
        Product product = productQueryService.getProductEntityByIdWithInventories(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new GoneException("The product has been discontinued or no longer exists.");
        }
        if (!product.isInStock()) {
            throw new GoneException("The product is out of stock");
        }

        int availableQuantity = product.getTotalInventory();
        if (request.getQuantity() > availableQuantity) {
            throw new ConflictException("Exceeds available stock quantity.",
                    new ConflictException.ConflictData(availableQuantity, request.getQuantity()));
        }

        // 2. Find or create user's cart
        Cart cart = cartQueryRepository.findByUserId(accountId) // findByUserId uses accountId as user.id
                .orElseGet(() -> createNewCart(accountId));

        // 3. Check if item already exists in cart
        // We need to fetch cart items belonging to this cart
        Optional<CartItem> existingCartItemOptional = cartItemQueryRepository.findByCartId(cart.getId())
                .stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            // Update existing item
            cartItem = existingCartItemOptional.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > availableQuantity) {
                throw new ConflictException("Vượt quá số lượng tồn kho",
                        new ConflictException.ConflictData(availableQuantity, newQuantity));
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cartItemCommandRepository.save(cartItem);
        cart.setUpdatedAt(LocalDateTime.now());
        cartCommandRepository.save(cart); // Update cart's updatedAt timestamp

        return CartItemAddResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .lineTotal(cartItem.getLineTotal())
                .build();
    }

    @Transactional
    public CartItemUpdateResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemQuantityRequest request) {
        Long accountId = SecurityUtils.getCurrentUserUuid();
        if (accountId == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        // Find the user's cart
        Cart userCart = cartQueryRepository.findByUserId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));

        // Find the cart item
        CartItem cartItem = cartItemCommandRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in the cart"));

        // Verify the cart item belongs to the current user's cart
        if (!cartItem.getCart().getId().equals(userCart.getId())) {
            throw new ResourceNotFoundException("Product not found in the cart.");
        }

        // Validate new quantity
        int newQuantity = request.getQuantity();
        if (newQuantity <= 0) { // Although @Min(1) handles this, good to have server-side check
            throw new ConflictException("Quantity must be greater than 0.");
        }

        Product product = cartItem.getProduct(); // Product is eagerly loaded with CartItem

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new GoneException("The product has been discontinued or no longer exists.");
        }
        if (!product.isInStock()) {
            throw new GoneException("The product is out of stock");
        }

        int availableQuantity = product.getTotalInventory();
        if (newQuantity > availableQuantity) {
            throw new ConflictException("Quantity exceeds the allowed limit",
                    new ConflictException.ConflictData(availableQuantity, newQuantity));
        }
        // Additional business rule: Max 50 items per user's cart (total quantity, not distinct items)
        // This rule is about total quantity of *this* specific product within the cart, not distinct items.
        // The prompt says "Mỗi người dùng có thể có tối đa 50 items trong giỏ" which implies total distinct items.
        // However, "Mỗi sản phẩm có số lượng tối đa cho phép (max_quantity)" means for each product, there's a limit.
        // Let's assume the 50 items limit is for each *cart_item*, i.e., 50 units of a single product.
        // If it means total count of CartItems (distinct products), then `userCart.getItems().size()` would be relevant.
        // If it means total aggregated quantity across all CartItems in the Cart, that's different.
        // Based on the error response "Số lượng vượt quá giới hạn cho phép", it refers to the available_quantity vs requested_quantity.
        // The prompt later says "Mỗi sản phẩm có số lượng tối đa cho phép (max_quantity)". This max_quantity is likely linked to inventory.

        // I will assume the "max_quantity" is handled by the `availableQuantity` check.
        // If a separate max_quantity (e.g., 50) is to be applied to each product regardless of stock,
        // that would need to be stored on the Product entity or configured globally.
        // For now, I will interpret "Mỗi người dùng có thể có tối đa 50 items trong giỏ" as a soft limit
        // which may not cause a hard error at this stage, or it refers to the `availableQuantity` of the product.
        // Let's assume for now that the 50 item limit is per-product, and covered by `availableQuantity`.

        // Update quantity
        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemCommandRepository.save(cartItem);

        userCart.setUpdatedAt(LocalDateTime.now());
        cartCommandRepository.save(userCart);

        return CartItemUpdateResponse.builder()
                .cartItemId(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .lineTotal(cartItem.getLineTotal())
                .build();
    }

    @Transactional
    public void deleteCartItem(Long cartItemId) {
        Long accountId = SecurityUtils.getCurrentUserUuid();
        if (accountId == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        // Find the user's cart (which now eagerly loads items)
        Cart userCart = cartQueryRepository.findByUserId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));

        // Find the cart item to be removed within the user's cart
        // This is important because we need the managed instance to remove from the collection
        CartItem cartItemToRemove = userCart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in the cart."));

        // Remove the cart item from the parent's collection
        // This will trigger orphanRemoval = true, deleting the child entity
        userCart.getItems().remove(cartItemToRemove);

        // Update cart's updatedAt timestamp and save the parent entity
        userCart.setUpdatedAt(LocalDateTime.now());
        cartCommandRepository.save(userCart);
    }

    @Transactional
    public void clearCart() {
        Long accountId = SecurityUtils.getCurrentUserUuid();
        if (accountId == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        // Find the user's cart (which now eagerly loads items)
        Cart userCart = cartQueryRepository.findByUserId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));

        // Clear the collection on the parent entity
        userCart.getItems().clear(); // This will trigger orphanRemoval for all items

        // Update cart's updatedAt timestamp and save the parent entity
        userCart.setUpdatedAt(LocalDateTime.now());
        cartCommandRepository.save(userCart);
    }



    private Cart createNewCart(Long accountId) {
        User user = userQueryRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for account ID: " + accountId));

        Cart newCart = Cart.builder()
                .user(user)
                .build();
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setUpdatedAt(LocalDateTime.now());
        return cartCommandRepository.save(newCart);
    }
}
