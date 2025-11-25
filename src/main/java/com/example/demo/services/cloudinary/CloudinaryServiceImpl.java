package com.example.demo.services.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("🔄 Uploading image to Cloudinary: {}", file.getOriginalFilename());

        // Validate file
        validateImageFile(file);

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "transformation", new com.cloudinary.Transformation()
                            .width(1000)
                            .height(1000)
                            .crop("limit")
                            .quality("auto:good")
                            .fetchFormat("auto")
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            log.info("✅ Image uploaded successfully: {}", uploadResult.get("secure_url"));
            return uploadResult;

        } catch (IOException e) {
            log.error("❌ Failed to upload image to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Check file size (max 100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IOException("File size exceeds maximum limit of 100MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }

        // Allow only specific image formats
        String[] allowedFormats = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isValidFormat = false;
        for (String format : allowedFormats) {
            if (format.equals(contentType)) {
                isValidFormat = true;
                break;
            }
        }

        if (!isValidFormat) {
            throw new IOException("Invalid image format. Allowed formats: JPEG, PNG, GIF, WEBP");
        }
    }
}
