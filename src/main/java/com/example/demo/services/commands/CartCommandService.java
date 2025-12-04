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
    import com.example.demo.exceptions.UnauthorizedException;
    import com.example.demo.repositories.commands.CartCommandRepository;
    import com.example.demo.repositories.commands.CartItemCommandRepository;
    import com.example.demo.repositories.queries.CartQueryRepository;
    import com.example.demo.repositories.queries.CartItemQueryRepository;
    import com.example.demo.repositories.queries.UserQueryRepository;
    import com.example.demo.services.queries.ProductQueryService;
    import com.example.demo.utils.SecurityUtils;
    import jakarta.persistence.EntityManager; // Re-inject EntityManager
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
        private final UserQueryRepository userQueryRepository;
        private final EntityManager entityManager; // Inject EntityManager // Inject UserQueryRepository
    
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
        public CartItemUpdateResponse updateCartItemQuantity(
                Long cartItemId,
                UpdateCartItemQuantityRequest request) {

            Long accountId = SecurityUtils.getCurrentUserUuid();
            if (accountId == null) {
                throw new UnauthorizedException("User is not authenticated.");
            }


            log.info("🔄 START: updateCartItemQuantity");
            log.info("  - cartItemId: {}", cartItemId);
            log.info("  - newQuantity: {}", request.getQuantity());
            log.info("  - accountId: {}", accountId);


            try {
                // Step 1: Verify cart exists
                log.info("1️⃣ Verifying user cart exists...");
                Cart userCart = cartQueryRepository.findByUserId(accountId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));
                log.info("✓ User cart found - Cart ID: {}", userCart.getId());

                // Step 2: Find cart item
                log.info("2️⃣ Finding cart item with ID: {}", cartItemId);
                CartItem cartItem = cartItemCommandRepository.findById(cartItemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found in the cart"));
                log.info("✓ Cart item found");
                log.info("  - Current quantity (before): {}", cartItem.getQuantity());
                log.info("  - Unit price: {}", cartItem.getUnitPrice());
                log.info("  - Cart ID of item: {}", cartItem.getCart().getId());

                // Step 3: Verify ownership
                log.info("3️⃣ Verifying cart item belongs to user's cart...");
                if (!cartItem.getCart().getId().equals(userCart.getId())) {
                    log.error("❌ Cart item does not belong to user's cart!");
                    throw new ResourceNotFoundException("Product not found in the cart.");
                }
                log.info("✓ Ownership verified");

                // Step 4: Validate new quantity
                int newQuantity = request.getQuantity();
                log.info("4️⃣ Validating new quantity: {}", newQuantity);
                if (newQuantity <= 0) {
                    throw new ConflictException("Quantity must be greater than 0.");
                }

                // Step 5: Validate product
                Product product = cartItem.getProduct();
                log.info("5️⃣ Validating product...");
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
                log.info("✓ Product validation passed");

                // Step 6: Update quantity
                log.info("6️⃣ Updating quantity in memory...");
                log.info("  - Old quantity: {}", cartItem.getQuantity());
                cartItem.setQuantity(newQuantity);
                log.info("  - New quantity (in memory): {}", cartItem.getQuantity());
                cartItem.setUpdatedAt(LocalDateTime.now());

                // Step 7: Save to database
                log.info("7️⃣ Saving to database...");
                CartItem savedCartItem = cartItemCommandRepository.save(cartItem);
                log.info("✓ Save executed");
                log.info("  - Saved quantity (from save): {}", savedCartItem.getQuantity());

                // Step 8: Flush to force SQL execution
                log.info("8️⃣ Flushing to force SQL UPDATE...");
                entityManager.flush();
                log.info("✓ Flush completed - SQL UPDATE should be executed now");

                // Step 9: Verify in database by querying again
                log.info("9️⃣ Verifying update in database by querying...");
                CartItem verifyFromDb = cartItemCommandRepository.findById(cartItemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cart item disappeared!"));
                log.info("✓ Database verification:");
                log.info("  - Quantity in DB: {}", verifyFromDb.getQuantity());
                if (verifyFromDb.getQuantity() != newQuantity) {
                    log.error("❌ DATABASE UPDATE FAILED!");
                    log.error("  Expected: {}, Actual in DB: {}", newQuantity, verifyFromDb.getQuantity());
                    throw new RuntimeException("Database did not persist the quantity update!");
                }

                // Step 10: Build response
                log.info("🔟 Building response...");
                CartItemUpdateResponse response = CartItemUpdateResponse.builder()
                        .cartItemId(savedCartItem.getId())
                        .quantity(savedCartItem.getQuantity())
                        .lineTotal(savedCartItem.getLineTotal())
                        .build();


                log.info("✅ COMPLETE: updateCartItemQuantity");
                log.info("  - Final response quantity: {}", response.getQuantity());
                log.info("  - Final response lineTotal: {}", response.getLineTotal());


                return response;

            } catch (Exception e) {

                log.error("❌ ERROR in updateCartItemQuantity");
                log.error("Exception Type: {}", e.getClass().getSimpleName());
                log.error("Exception Message: {}", e.getMessage());
                log.error("Stack trace:", e);

                throw e;
            }
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
