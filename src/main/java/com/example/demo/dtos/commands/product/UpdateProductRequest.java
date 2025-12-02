package com.example.demo.dtos.commands.product;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateProductRequest extends WriteProductRequest{
    private MultipartFile image;

    private String images;
}
