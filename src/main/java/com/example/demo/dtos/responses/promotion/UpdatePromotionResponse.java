package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePromotionResponse {
    private Long id;
    private String code;
    private LocalDateTime updated_at;
}
