package com.example.demo.dtos.responses.promotion;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePromotionResponse {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private LocalDateTime created_at;

}
