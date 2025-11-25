package com.example.demo.dtos.responses.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadImageResponse {
    private Long id;
    private String url;
}
