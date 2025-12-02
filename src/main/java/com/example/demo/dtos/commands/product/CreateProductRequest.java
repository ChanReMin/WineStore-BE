package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateProductRequest extends WriteProductRequest{
    @NotNull(message = "Product image is required")
    private MultipartFile image;
}
