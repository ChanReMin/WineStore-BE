package com.example.demo.dtos.responses.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class SellerOrderListItemResponse {
    private Long id;
    @JsonProperty("order_code")
    private String orderCode;
    private CustomerDetailResponse customer;
    private int status;
    @JsonProperty("status_text")
    private String statusText;
    @JsonProperty("payment_status")
    private int paymentStatus;
    @JsonProperty("payment_status_text")
    private String paymentStatusText;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    @JsonProperty("final_amount")
    private BigDecimal finalAmount;
    @JsonProperty("items_count")
    private int itemsCount;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("time_remaining_to_confirm")
    private Long timeRemainingToConfirm; // in seconds
}