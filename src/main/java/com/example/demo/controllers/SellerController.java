package com.example.demo.controllers;

import com.example.demo.dtos.queries.order.OrderQueryRequest;
import com.example.demo.dtos.responses.PaginationResponse;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.order.OrderResponse;
import com.example.demo.dtos.responses.order.SellerOrderDetailResponse;
import com.example.demo.dtos.responses.order.SellerOrderListResponse;
import com.example.demo.services.queries.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerController {
    private final OrderQueryService orderQueryService;

    @GetMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<SellerOrderDetailResponse>> getOrderDetailForSeller(@PathVariable Long orderId) {
        SellerOrderDetailResponse orderDetail = orderQueryService.getOrderDetailForSeller(orderId);
        return new ResponseEntity<>(new SuccessResponse<>(true, "Order details retrieved successfully", orderDetail), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<SellerOrderListResponse>> getOrdersForSeller(@ModelAttribute OrderQueryRequest request) {
        SellerOrderListResponse response = orderQueryService.getOrdersForSeller(request);
        return new ResponseEntity<>(new SuccessResponse<>(true, "Orders retrieved successfully", response), HttpStatus.OK);
    }
}
