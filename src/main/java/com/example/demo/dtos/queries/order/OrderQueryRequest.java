package com.example.demo.dtos.queries.order;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class OrderQueryRequest {
    private Integer page = 1;
    private Integer limit = 20;
    private Integer status; // Corresponds to OrderStatus enum value
    private Integer paymentStatus; // Corresponds to PaymentStatus enum value

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String search; // Search by order_code
}
