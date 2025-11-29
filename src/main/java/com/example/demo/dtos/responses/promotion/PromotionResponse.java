package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private int discount_type;
    private String discount_type_text;
    private double discount_value;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int max_usage;
    private int used_count;
    private int remaining_usage;
    private int status;
    private String status_text;
    private int applicable_products_count;
    private LocalDateTime created_at;

}
