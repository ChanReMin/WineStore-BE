package com.example.demo.controllers;

import com.example.demo.dtos.commands.order.OrderCreateRequest;
import com.example.demo.dtos.queries.order.OrderQueryRequest;
import com.example.demo.dtos.responses.PaginationResponse;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.order.*;
import com.example.demo.services.commands.OrderCommandService;
import com.example.demo.services.queries.OrderQueryService;
import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dtos.responses.order.OrderResponse;
import com.example.demo.dtos.responses.order.SellerOrderDetailResponse;
import com.example.demo.dtos.responses.order.SellerOrderListResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<SuccessResponse<OrderCreateResponse>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderCreateResponse response = orderCommandService.createOrder(request);
        return new ResponseEntity<>(new SuccessResponse<OrderCreateResponse>(true, "Order created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getOrdersForCustomer(@ModelAttribute OrderQueryRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();
        Page<OrderResponse> ordersPage = orderQueryService.getOrdersForCustomer(currentUserId, request);

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(ordersPage.getNumber() + 1)
                .totalPages(ordersPage.getTotalPages())
                .totalItems(ordersPage.getTotalElements())
                .perPage(ordersPage.getSize())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("orders", ordersPage.getContent());
        data.put("pagination", pagination);

        return new ResponseEntity<>(new SuccessResponse<>(true, "Orders retrieved successfully", data), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<OrderDetailResponse>> getOrderDetailForCustomer(@PathVariable Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();
        OrderDetailResponse orderDetail = orderQueryService.getOrderDetailForCustomer(currentUserId, orderId);
        return new ResponseEntity<>(new SuccessResponse<>(true, "Order details retrieved successfully", orderDetail), HttpStatus.OK);
    }

    @GetMapping("/{orderId}/seller")
    public ResponseEntity<SuccessResponse<SellerOrderDetailResponse>> getOrderDetailForSeller(@PathVariable Long orderId) {
        SellerOrderDetailResponse orderDetail = orderQueryService.getOrderDetailForSeller(orderId);
        return new ResponseEntity<>(new SuccessResponse<>(true, "Order details retrieved successfully", orderDetail), HttpStatus.OK);
    }

    @GetMapping("/seller")
    public ResponseEntity<SuccessResponse<SellerOrderListResponse>> getOrdersForSeller(@ModelAttribute OrderQueryRequest request) {
        SellerOrderListResponse response = orderQueryService.getOrdersForSeller(request);
        return new ResponseEntity<>(new SuccessResponse<>(true, "Orders retrieved successfully", response), HttpStatus.OK);
    }

}
