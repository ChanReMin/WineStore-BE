package com.example.demo.dtos.commands;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadProductImageRequest {
    private MultipartFile image;
}
