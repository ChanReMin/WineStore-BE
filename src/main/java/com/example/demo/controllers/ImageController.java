package com.example.demo.controllers;

import com.example.demo.dtos.commands.UploadProductImageRequest;
import com.example.demo.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@ModelAttribute UploadProductImageRequest request) throws IOException {
        MultipartFile image = request.getImage();

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("No image provided");
        }

        // Upload to Cloudinary
        String result = cloudinaryService.uploadImage(image);

        // Return secure URL
        return ResponseEntity.ok(result);
    }
}
