package com.example.demo.dtos.commands.promotion;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchRequest {
    private Integer page;
    private Integer limit;
    private Integer status;
    private String search;
    private String sortBy;
    private String sortOrder;
}
