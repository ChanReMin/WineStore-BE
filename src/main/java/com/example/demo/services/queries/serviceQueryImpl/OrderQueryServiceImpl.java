package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.commons.enums.PaymentStatus;
import com.example.demo.dtos.queries.order.OrderQueryRequest;
import com.example.demo.dtos.responses.PaginationResponse;
import com.example.demo.dtos.responses.order.CouponDetailResponse;
import com.example.demo.dtos.responses.order.CustomerDetailResponse;
import com.example.demo.dtos.responses.order.OrderDetailResponse;
import com.example.demo.dtos.responses.order.OrderItemDetailResponse;
import com.example.demo.dtos.responses.order.OrderResponse;
import com.example.demo.dtos.responses.order.OrderSummaryResponse;
import com.example.demo.dtos.responses.order.OrderTimelineEventResponse;
import com.example.demo.dtos.responses.order.PaymentMethodDetailResponse;
import com.example.demo.dtos.responses.order.SellerOrderDetailResponse;
import com.example.demo.dtos.responses.order.SellerOrderListResponse;
import com.example.demo.dtos.responses.order.SellerOrderListItemResponse;
import com.example.demo.dtos.responses.order.SellerOrderItemDetailResponse;
import com.example.demo.dtos.responses.order.ShippingAddressDetailResponse;
import com.example.demo.dtos.responses.order.ShippingAddressResponse;
import com.example.demo.dtos.responses.order.ShippingInfoResponse;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.PaymentTransaction;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.entities.UserAddress;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.OrderQueryRepository;
import com.example.demo.services.queries.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    @Override
    public Page<OrderResponse> getOrdersForCustomer(Long userId, OrderQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit());

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by user ID
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            // Filter by status
            if (request.getStatus() != null) {
                Optional.ofNullable(OrderStatus.fromValue(request.getStatus()))
                        .ifPresent(status -> predicates.add(criteriaBuilder.equal(root.get("status"), status)));
            }

            // Filter by payment status
            if (request.getPaymentStatus() != null) {
                Optional.ofNullable(PaymentStatus.fromValue(request.getPaymentStatus()))
                        .ifPresent(paymentStatus -> predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus)));
            }

            // Filter by from_date
            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate().atStartOfDay()));
            }

            // Filter by to_date
            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getToDate().atStartOfDay().plusDays(1).minusNanos(1)));
            }

            // Search by order_code
            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), "%" + request.getSearch().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return orderQueryRepository.findAll(spec, pageable).map(this::mapToOrderResponse);
    }

    @Override
    public OrderDetailResponse getOrderDetailForCustomer(Long userId, Long orderId) {
        Order order = orderQueryRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or does not belong to user"));

        return mapToOrderDetailResponse(order);

    }

    @Override
    public SellerOrderListResponse getOrdersForSeller(OrderQueryRequest request) {
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit());

        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getStatus() != null) {
                Optional.ofNullable(OrderStatus.fromValue(request.getStatus()))
                        .ifPresent(status -> predicates.add(criteriaBuilder.equal(root.get("status"), status)));
            }

            if (request.getPaymentStatus() != null) {
                Optional.ofNullable(PaymentStatus.fromValue(request.getPaymentStatus()))
                        .ifPresent(paymentStatus -> predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus)));
            }

            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate().atStartOfDay()));
            }

            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getToDate().atStartOfDay().plusDays(1).minusNanos(1)));
            }

            if (request.getSearch() != null && !request.getSearch().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), "%" + request.getSearch().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> ordersPage = orderQueryRepository.findAll(spec, pageable);

        List<SellerOrderListItemResponse> orderListItems = ordersPage.getContent().stream()
                .map(this::mapToSellerOrderListItemResponse)
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(ordersPage.getNumber() + 1)
                .totalPages(ordersPage.getTotalPages())
                .totalItems(ordersPage.getTotalElements())
                .perPage(ordersPage.getSize())
                .build();

        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .pending(orderQueryRepository.countByStatus(OrderStatus.PENDING))
                .processing(orderQueryRepository.countByStatus(OrderStatus.PAID))
                .shipping(orderQueryRepository.countByStatus(OrderStatus.CONFIRMED))
                .completed(0L) // No direct equivalent in simplified enum
                .cancelled(orderQueryRepository.countByStatus(OrderStatus.CANCELLED))
                .build();

        return SellerOrderListResponse.builder()
                .orders(orderListItems)
                .pagination(pagination)
                .summary(summary)
                .build();
    }

    private SellerOrderListItemResponse mapToSellerOrderListItemResponse(Order order) {
        // Ensure account is not null before accessing email
        String customerEmail = (order.getUser() != null && order.getUser().getAccount() != null)
                ? order.getUser().getAccount().getEmail()
                : null;
        String customerPhone = (order.getUser() != null) ? order.getUser().getPhoneNumber() : null;

        CustomerDetailResponse customerDetailResponse = CustomerDetailResponse.builder()
                .id(order.getUser().getId())
                .name(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .email(customerEmail)
                .phone(customerPhone)
                // totalOrders is not needed for list items, only for detail
                .build();

        // Calculate time remaining to confirm (assuming 24 hours for confirmation)
        Long timeRemainingToConfirm = null;
        if (order.getStatus() == OrderStatus.PENDING) {
            LocalDateTime confirmDeadline = order.getCreatedAt().plusHours(24);
            if (confirmDeadline.isAfter(LocalDateTime.now())) {
                timeRemainingToConfirm = Duration.between(LocalDateTime.now(), confirmDeadline).getSeconds();
            } else {
                timeRemainingToConfirm = 0L; // Deadline passed
            }
        }

        return SellerOrderListItemResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customer(customerDetailResponse)
                .status(order.getStatus().getValue())
                .statusText(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().getValue())
                .paymentStatusText(order.getPaymentStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .itemsCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
                .createdAt(order.getCreatedAt().atOffset(ZoneOffset.UTC))
                .timeRemainingToConfirm(timeRemainingToConfirm)
                .build();
    }


    @Override
    public SellerOrderDetailResponse getOrderDetailForSeller(Long orderId) {
        Order order = orderQueryRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToSellerOrderDetailResponse(order);
    }

    private SellerOrderDetailResponse mapToSellerOrderDetailResponse(Order order) {
        List<SellerOrderItemDetailResponse> itemDetails = order.getOrderItems().stream()
                .map(this::mapToSellerOrderItemDetailResponse)
                .collect(Collectors.toList());

        BigDecimal totalProfit = itemDetails.stream()
                .map(SellerOrderItemDetailResponse::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User customer = order.getUser();
        CustomerDetailResponse customerDetailResponse = CustomerDetailResponse.builder()
                .id(customer.getId())
                .name(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getAccount().getEmail())
                .phone(customer.getPhoneNumber())
                .totalOrders(orderQueryRepository.countByUserId(customer.getId()))
                .build();

        ShippingAddressDetailResponse shippingAddressDetail = null;
        if (order.getShippingAddress() != null) {
            shippingAddressDetail = mapToShippingAddressDetailResponse(order.getShippingAddress());
        }

        return SellerOrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customer(customerDetailResponse)
                .status(order.getStatus().getValue())
                .statusText(order.getStatus().name())
                .items(itemDetails)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .totalProfit(totalProfit)
                .shippingAddress(shippingAddressDetail)
                .internalNote(null) // Not implemented
                .build();
    }

    private SellerOrderItemDetailResponse mapToSellerOrderItemDetailResponse(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        BigDecimal profit = (orderItem.getUnitPrice().subtract(costPrice)).multiply(new BigDecimal(orderItem.getQuantity()));

        Long warehouseId = null;
        if (product.getInventories() != null && !product.getInventories().isEmpty()) {
            warehouseId = product.getInventories().get(0).getWarehouse().getId();
        }


        return SellerOrderItemDetailResponse.builder()
                .id(orderItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .costPrice(costPrice)
                .lineTotal(orderItem.getLineTotal())
                .profit(profit)
                .warehouseId(warehouseId)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        ShippingAddressResponse shippingAddressResponse = null;
        if (order.getShippingAddress() != null) {
            UserAddress address = order.getShippingAddress();
            shippingAddressResponse = ShippingAddressResponse.builder()
                    .fullName(address.getFullName())
                    .phoneNumber(address.getPhoneNumber())
                    .addressLine(address.getAddressLine())
                    .city(address.getCity())
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().getValue())
                .statusText(order.getStatus().getDescription())
                .paymentStatus(order.getPaymentStatus().getValue())
                .paymentStatusText(order.getPaymentStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(BigDecimal.ZERO) // Hardcoded as 0 for now as per previous discussion
                .finalAmount(order.getFinalAmount())
                .itemsCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
                .createdAt(order.getCreatedAt().atOffset(ZoneOffset.UTC))
                .paidAt(order.getPaidAt() != null ? order.getPaidAt().atOffset(ZoneOffset.UTC) : null)
                .shippingAddress(shippingAddressResponse)
                .build();
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        // Map OrderItem entities to OrderItemDetailResponse DTOs
        List<OrderItemDetailResponse> itemDetails = order.getOrderItems().stream()
                .map(this::mapToOrderItemDetailResponse)
                .collect(Collectors.toList());

        // Map ShippingAddress entity to ShippingAddressDetailResponse DTO
        ShippingAddressDetailResponse shippingAddressDetail = null;
        if (order.getShippingAddress() != null) {
            shippingAddressDetail = mapToShippingAddressDetailResponse(order.getShippingAddress());
        }

        // Map PaymentMethod entity to PaymentMethodDetailResponse DTO
        PaymentMethodDetailResponse paymentMethodDetail = null;
        if (order.getPaymentTransactions() != null && !order.getPaymentTransactions().isEmpty()) {
            // Assuming the first payment transaction's method is the one used for the order
            PaymentTransaction firstTransaction = order.getPaymentTransactions().stream().findFirst().orElse(null);
            if (firstTransaction != null && firstTransaction.getPaymentMethod() != null) {
                paymentMethodDetail = mapToPaymentMethodDetailResponse(firstTransaction.getPaymentMethod());
            }
        }

        // Coupon details (currently not directly linked to Order entity)
        CouponDetailResponse couponDetail = null;
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            couponDetail = CouponDetailResponse.builder()
                    .discountAmount(order.getDiscountAmount())
                    // Fields like code, type, value are not available from Order entity
                    .build();
        }

        // Shipping Info (currently no ShippingInfo entity in Order)
        ShippingInfoResponse shippingInfo = null;

        // Timeline (construct from order dates)
        List<OrderTimelineEventResponse> timeline = buildOrderTimeline(order);

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().getValue())
                .statusText(order.getStatus().getDescription())
                .paymentStatus(order.getPaymentStatus().getValue())
                .paymentStatusText(order.getPaymentStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(BigDecimal.ZERO) // Hardcoded as 0 for now
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt().atOffset(ZoneOffset.UTC))
                .paidAt(order.getPaidAt() != null ? order.getPaidAt().atOffset(ZoneOffset.UTC) : null)
                .confirmedAt(null) // Not available in Order entity
                .shippedAt(null) // Not available in Order entity
                .note(null) // Not available in Order entity
                .items(itemDetails)
                .shippingAddress(shippingAddressDetail)
                .paymentMethod(paymentMethodDetail)
                .coupon(couponDetail)
                .shippingInfo(shippingInfo)
                .timeline(timeline)
                .build();
    }

    private OrderItemDetailResponse mapToOrderItemDetailResponse(OrderItem orderItem) {
        // Assuming product details are available from eagerly loaded OrderItem.product
        return OrderItemDetailResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .productSlug(orderItem.getProduct().getSlug())
                .productImage(orderItem.getProduct().getImages()) // Assuming images field holds a single image URL or a representative one
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }

    private ShippingAddressDetailResponse mapToShippingAddressDetailResponse(UserAddress address) {
        return ShippingAddressDetailResponse.builder()
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .build();
    }

    private PaymentMethodDetailResponse mapToPaymentMethodDetailResponse(com.example.demo.entities.PaymentMethod paymentMethod) {
        return PaymentMethodDetailResponse.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .code(paymentMethod.getCode())
                .build();
    }

    private List<OrderTimelineEventResponse> buildOrderTimeline(Order order) {
        List<OrderTimelineEventResponse> timeline = new ArrayList<>();

        // Basic timeline from available order dates
        if (order.getCreatedAt() != null) {
            timeline.add(OrderTimelineEventResponse.builder()
                    .status(OrderStatus.PENDING.getValue())
                    .statusText(OrderStatus.PENDING.getDescription())
                    .timestamp(order.getCreatedAt().atOffset(ZoneOffset.UTC))
                    .note("Order created")
                    .build());
        }
        // Confirmed at, Shipped at are not available in Order entity
        // If order status changes include these events, they would be added here
        // based on Order entity status field transitions and corresponding dates if they existed.

        if (order.getPaidAt() != null) {
            timeline.add(OrderTimelineEventResponse.builder()
                    .status(OrderStatus.PAID.getValue()) // Assuming PAID is a distinct status in timeline
                    .statusText(OrderStatus.PAID.getDescription())
                    .timestamp(order.getPaidAt().atOffset(ZoneOffset.UTC))
                    .note("Payment received")
                    .build());
        }

        // Sort timeline events by timestamp
        timeline.sort((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()));

        return timeline;
    }
}