package com.example.demo.services.commands;

import com.example.demo.dtos.commands.order.OrderCreateRequest;
import com.example.demo.dtos.responses.order.OrderCreateResponse;

public interface OrderCommandService {
    OrderCreateResponse createOrder(OrderCreateRequest request);
}
