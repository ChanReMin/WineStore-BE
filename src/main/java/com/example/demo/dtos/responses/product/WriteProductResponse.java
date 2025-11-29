package com.example.demo.dtos.responses.product;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriteProductResponse {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private LocalDateTime created_at;

}
