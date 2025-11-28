package com.example.demo.dtos.responses.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProductStatusResponse {
    private Long id;
    private Integer status;
    private String statusText;
}