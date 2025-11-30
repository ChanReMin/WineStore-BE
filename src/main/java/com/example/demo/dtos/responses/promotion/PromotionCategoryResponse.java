package com.example.demo.dtos.responses.promotion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCategoryResponse {
    private Long id;
    private String name;
}
