package com.example.demo.dtos.responses.category;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListItemResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long productsCount;
}
