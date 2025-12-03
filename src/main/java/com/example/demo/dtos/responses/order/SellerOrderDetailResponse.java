package com.example.demo.dtos.responses.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SellerOrderDetailResponse {
    private Long id;
    @JsonProperty("order_code")
    private String orderCode;
    private CustomerDetailResponse customer;
    private int status;
    @JsonProperty("status_text")
    private String statusText;
    private List<SellerOrderItemDetailResponse> items;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    @JsonProperty("final_amount")
    private BigDecimal finalAmount;
    @JsonProperty("total_profit")
    private BigDecimal totalProfit;
    @JsonProperty("shipping_address")
    private ShippingAddressDetailResponse shippingAddress;
    private String note;
    @JsonProperty("internal_note")
    private String internalNote;
}
