package com.example.demo.dtos.responses.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductResponse {
    private Long id;
    private String name;
    private Integer status;
    private String statusText;
}
