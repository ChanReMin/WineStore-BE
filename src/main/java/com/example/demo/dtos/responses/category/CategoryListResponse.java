package com.example.demo.dtos.responses.category;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryListResponse {
    private List<CategoryListItemResponse> categories;
}
