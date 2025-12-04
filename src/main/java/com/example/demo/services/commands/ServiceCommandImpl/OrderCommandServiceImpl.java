package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.commons.enums.PaymentStatus;
import com.example.demo.dtos.commands.order.ChangeOrderStatusRequest;
import com.example.demo.dtos.commands.order.OrderCreateRequest;
import com.example.demo.dtos.responses.order.ChangeOrderStatusResponse;
import com.example.demo.dtos.responses.order.OrderCreateResponse;
import com.example.demo.dtos.responses.order.UnavailableProductResponse;
import com.example.demo.entities.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.OrderConflictException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.CartCommandRepository;
import com.example.demo.repositories.commands.OrderCommandRepository;
import com.example.demo.repositories.commands.OrderItemCommandRepository;
import com.example.demo.repositories.queries.*;
import com.example.demo.services.queries.PaymentService;
import com.example.demo.services.PaymentServiceFactory;
import com.example.demo.services.commands.OrderCommandService;
import com.example.demo.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandServiceImpl.class);

    private final UserQueryRepository userRepository;
    private final CartQueryRepository cartQueryRepository;
    private final CartCommandRepository cartCommandRepository; // To clear cart
    private final UserAddressQueryRepository userAddressQueryRepository;
    private final PaymentMethodQueryRepository paymentMethodQueryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final InventoryQueryRepository inventoryQueryRepository; // For fetching inventory
    private final OrderCommandRepository orderCommandRepository;
    private final OrderItemCommandRepository orderItemCommandRepository;
    private final PromotionQueryRepository promotionQueryRepository;
    private final PaymentServiceFactory paymentServiceFactory;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        log.info("Starting order creation for request: {}", request);

        // 1. Get current authenticated user
        Long currentUserId = SecurityUtils.getCurrentUserUuid();
        log.debug("Current authenticated user ID: {}", currentUserId);
        User currentUser = userRepository.findByAccountId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.debug("User found: {}", currentUser.getId());

        // 2. Fetch user's cart and cart items
        Cart cart = cartQueryRepository.findByUser(currentUser)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        log.debug("User cart fetched. Cart ID: {}, Number of items: {}", cart.getId(), cart.getItems().size());

        if (cart.getItems().isEmpty()) {
            log.warn("Cart is empty for user: {}", currentUserId);
            throw new BadRequestException("Cart is empty");
        }

        // 3. Validate: Shipping address
        UserAddress shippingAddress = userAddressQueryRepository.findById(request.getShippingAddressId())
                .filter(address -> address.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> {
                    log.warn("Shipping address not found or does not belong to user {}. Address ID: {}", currentUserId, request.getShippingAddressId());
                    return new ResourceNotFoundException("Shipping address not found or does not belong to user");
                });
        log.debug("Shipping address validated. Address ID: {}", shippingAddress.getId());

        // 4. Validate: Payment method
        PaymentMethod paymentMethod = paymentMethodQueryRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> {
                    log.warn("Payment method not found. Method ID: {}", request.getPaymentMethodId());
                    return new ResourceNotFoundException("Payment method not found");
                });
        log.debug("Payment method validated. Method ID: {}", paymentMethod.getId());


        // 5. Check product availability and inventory
        log.debug("Checking product availability and inventory for {} cart items.", cart.getItems().size());
        List<OrderItem> orderItems = new ArrayList<>();
        List<UnavailableProductResponse> unavailableItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productQueryRepository.findByIdWithInventories(cartItem.getProduct().getId())
                    .orElseThrow(() -> {
                        log.warn("Product not found during cart item processing. Product ID: {}", cartItem.getProduct().getId());
                        return new ResourceNotFoundException("Product not found: " + cartItem.getProduct().getId());
                    });
            log.debug("Processing cart item - Product ID: {}, Name: {}, Requested Quantity: {}",
                    product.getId(), product.getName(), cartItem.getQuantity());

            // Check product status (must be ACTIVE)
            if (product.getStatus() != com.example.demo.commons.enums.ProductStatus.ACTIVE) {
                log.warn("Product {} is not active. Status: {}", product.getId(), product.getStatus());
                unavailableItems.add(UnavailableProductResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .requestedQuantity(cartItem.getQuantity())
                        .availableQuantity(0)
                        .build());
                continue;
            }

            // Get total inventory quantity for the product
            Integer availableQuantity = product.getTotalInventory();

            if (availableQuantity < cartItem.getQuantity()) {
                log.warn("Insufficient stock for product {}. Requested: {}, Available: {}",
                        product.getId(), cartItem.getQuantity(), availableQuantity);
                unavailableItems.add(UnavailableProductResponse.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .requestedQuantity(cartItem.getQuantity())
                        .availableQuantity(availableQuantity)
                        .build());
            } else {
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .build();
                orderItem.calculateLineTotal(); // Ensure line total is calculated
                orderItems.add(orderItem);
                subTotal = subTotal.add(orderItem.getLineTotal());
                log.debug("Added order item for product {}. Line total: {}", product.getId(), orderItem.getLineTotal());
            }
        }

        if (!unavailableItems.isEmpty()) {
            log.warn("Order conflict due to unavailable items: {}", unavailableItems);
            throw new OrderConflictException("Some products in your cart are unavailable or out of stock", unavailableItems);
        }
        log.info("Product availability and inventory check completed successfully.");

        // 6. Calculate amounts and apply promotions
        log.debug("Calculating amounts and applying promotions.");
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            log.debug("Coupon code provided: {}", request.getCouponCode());
            Optional<Promotion> promotionOptional = promotionQueryRepository.findByCode(request.getCouponCode());
            if (promotionOptional.isPresent()) {
                Promotion promotion = promotionOptional.get();
                if (promotion.canBeUsed()) {
                    discountAmount = promotion.calculateDiscount(subTotal);
                    log.info("Promotion applied. Code: {}, Discount: {}", request.getCouponCode(), discountAmount);
                } else {
                    log.info("Promotion {} cannot be used at this time.", request.getCouponCode());
                    // Optionally, throw an exception or log that coupon cannot be used
                }
            } else {
                log.info("Invalid coupon code: {}", request.getCouponCode());
                // Optionally, throw an exception or log that coupon is invalid
            }
        }

        BigDecimal shippingFee = BigDecimal.ZERO; // As per requirement, shipping fee is 0
        BigDecimal finalAmount = subTotal.subtract(discountAmount).add(shippingFee);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO; // Ensure final amount is not negative
            log.warn("Final amount calculated to be negative, set to zero.");
        }
        log.info("Amounts calculated: SubTotal={}, Discount={}, ShippingFee={}, FinalAmount={}",
                subTotal, discountAmount, shippingFee, finalAmount);

        // 7. Generate unique order code (ORD-YYYYMMDD-XXXX)
        String orderCode = generateOrderCode();
        log.debug("Generated order code: {}", orderCode);

        // 8. Create and persist Order entity
        Order order = Order.builder()
                .user(currentUser)
                .shippingAddress(shippingAddress)
                .orderCode(orderCode)
                .status(OrderStatus.PENDING) // Initial status
                .totalAmount(subTotal)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .paymentStatus(PaymentStatus.UNPAID) // Initial payment status
                .build();
        order = orderCommandRepository.save(order);
        log.info("Order entity saved. Order ID: {}", order.getId());

        // 9. Create and persist OrderItem entities for each cart item
        Order finalOrder = order;
        orderItems.forEach(item -> {
            item.setOrder(finalOrder);
            orderItemCommandRepository.save(item);
            log.debug("Order item saved for product {}. Order ID: {}", item.getProduct().getId(), finalOrder.getId());
        });
        finalOrder.setOrderItems(new HashSet<>(orderItems)); // Link order items to order
        log.info("Order items saved and linked to order.");

        // 10. Update inventory: decrement quantity for each ordered product
        log.debug("Updating inventory for ordered products.");
        for (OrderItem item : orderItems) {
            Product product = item.getProduct(); // Product already eager loaded with inventories
            List<Inventory> productInventories = product.getInventories(); // Use eager loaded inventories
            int remainingQuantityToDeduct = item.getQuantity();

            if (productInventories == null || productInventories.isEmpty()) {
                log.error("No inventories found for product {} during deduction. This should not happen after initial check.", product.getId());
                throw new IllegalStateException("No inventory found for product: " + product.getId());
            }

            for (Inventory inventory : productInventories) {
                if (remainingQuantityToDeduct <= 0) break;

                int quantityInThisInventory = inventory.getQuantityOnHand();
                if (quantityInThisInventory >= remainingQuantityToDeduct) {
                    inventory.setQuantityOnHand(quantityInThisInventory - remainingQuantityToDeduct);
                    remainingQuantityToDeduct = 0;
                    log.debug("Deducted {} from inventory {} for product {}", item.getQuantity(), inventory.getId(), product.getId());
                } else {
                    remainingQuantityToDeduct -= quantityInThisInventory;
                    inventory.setQuantityOnHand(0);
                    log.debug("Deducted {} from inventory {} for product {}. Remaining to deduct: {}",
                            quantityInThisInventory, inventory.getId(), product.getId(), remainingQuantityToDeduct);
                }
                // No explicit save needed for inventory within a @Transactional method if the entity is managed
            }
            // If remainingQuantityToDeduct > 0, it means there was an inconsistency (should have been caught earlier)
            if (remainingQuantityToDeduct > 0) {
                log.error("Not enough stock despite initial check for product: {}. Still {} quantity remaining to deduct.", product.getId(), remainingQuantityToDeduct);
                throw new IllegalStateException("Not enough stock despite initial check for product: " + product.getId());
            }
        }
        log.info("Inventory updated successfully.");

        // 11. Clear user's cart
        cartCommandRepository.delete(cart); // Or clear items from cart.
        log.info("User cart cleared. Cart ID: {}", cart.getId());

        // 12. Handle payment based on method
        String paymentUrl = null;
        log.info("User cart cleared. Cart ID: {}", paymentMethod.getCode());
        if (!"COD".equalsIgnoreCase(paymentMethod.getCode())) {
            // For non-COD payments, generate a payment URL
            try {
                PaymentService paymentService = paymentServiceFactory.getPaymentService(paymentMethod.getCode());
                paymentUrl = paymentService.createPaymentUrl(httpServletRequest, order.getId(), finalAmount.longValue());
                log.debug("Generated payment URL: {}", paymentUrl);
            } catch (BadRequestException e) {
                log.error("Payment method '{}' is not supported.", paymentMethod.getCode(), e);
                // Re-throw the exception to inform the caller about the unsupported method
                throw e;
            }
        } else {
            // For COD, no payment URL is needed. The order is already created with UNPAID status.
            log.info("Order created with COD payment method. No payment URL generated. Order ID: {}", order.getId());
        }

        // 13. Return OrderCreateResponse
        log.info("Order creation completed successfully. Order ID: {}", order.getId());
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .totalAmount(subTotal) // Use subTotal before discount and shipping
                .discountAmount(discountAmount)
                .shippingFee(shippingFee) // Assuming 0 as per requirement
                .finalAmount(finalAmount)
                .status(order.getStatus().getValue())
                .statusText(order.getStatus().getDescription())
                .paymentStatus(order.getPaymentStatus().getValue())
                .paymentStatusText(order.getPaymentStatus().getDescription())
                .paymentUrl(paymentUrl)
                .createdAt(OffsetDateTime.now()) // Using current time for response
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public ChangeOrderStatusResponse updateStatusOrder(Long orderId, ChangeOrderStatusRequest request) {
        log.info("Updating order status. Order ID: {}, New Status: {}", orderId, request.getStatus());

        Order order = orderCommandRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.fromValue(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status value. Allowed values: 1 (Pending), 2 (Confirmed), 6 (Cancelled), 3 (Paid)"
            );
        }

        order.setStatus(newStatus);

        // Save updated product
        Order updatedOrder = orderCommandRepository.save(order);

        log.info("✅ Product status updated successfully - productId: {}, status: {}",
                orderId, newStatus.getDescription());

        return ChangeOrderStatusResponse.builder()
                .id(updatedOrder.getId())
                .status(updatedOrder.getStatus().getValue())
                .statusText(updatedOrder.getStatus(). getDescription())
                .build();
    }

    private String generateOrderCode() {
        // Format: ORD-YYYYMMDD-XXXX
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // This should ideally be a sequential number from a database sequence or similar to avoid collisions
        // For now, a simple random approach is used.
        String sequentialPart = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD-" + datePart + "-" + sequentialPart;
    }
}