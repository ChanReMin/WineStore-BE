package com.example.demo.services.queries;

import com.example.demo.dtos.queries.order.OrderQueryRequest;
import com.example.demo.dtos.responses.PaginationResponse;
import com.example.demo.dtos.responses.order.OrderDetailResponse;
import com.example.demo.dtos.responses.order.OrderResponse;
import com.example.demo.dtos.responses.order.SellerOrderDetailResponse;
import com.example.demo.dtos.responses.order.SellerOrderListResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

public interface OrderQueryService {
    Page<OrderResponse> getOrdersForCustomer(Long userId, OrderQueryRequest request);
    OrderDetailResponse getOrderDetailForCustomer(Long userId, Long orderId);

    SellerOrderDetailResponse getOrderDetailForSeller(Long orderId);

    SellerOrderListResponse getOrdersForSeller(OrderQueryRequest request);
}
