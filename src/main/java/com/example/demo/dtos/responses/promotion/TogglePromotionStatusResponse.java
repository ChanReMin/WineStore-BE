package com.example.demo.dtos.responses.promotion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TogglePromotionStatusResponse {
    private Long id;
    private Integer status;
    private String status_text;
}
