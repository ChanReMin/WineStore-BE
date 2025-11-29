package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionStatisticsResponse {
    private Long promotion_id;
    private String promotion_code;
    private String promotion_name;
    private Integer total_usage;
    private Integer max_usage;
    private Integer remaining_usage;
    private Double usage_rate; // (total_usage / max_usage) * 100
    private BigDecimal total_discount_amount;
    private Long total_orders;
    private BigDecimal total_revenue;
    private BigDecimal average_order_value; // total_revenue / total_orders
    private Long unique_customers;
    private Long new_customers; // Not directly supported by current entities, will be 0
    private Long returning_customers; // Not directly supported by current entities, will be 0
    private Double conversion_rate; // Not directly supported by current entities, will be 0.0
    private List<UsageByDate> usage_by_date;
    private List<TopProduct> top_products;
    private Period period;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsageByDate {
        private LocalDate date;
        private Integer usage_count;
        private BigDecimal discount_amount;
        private Long orders;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private Long product_id;
        private String product_name;
        private Integer usage_count;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Period {
        private LocalDateTime start_date;
        private LocalDateTime end_date;
    }
}
