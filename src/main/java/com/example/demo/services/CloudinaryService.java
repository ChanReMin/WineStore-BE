package com.example.demo.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.entities.Product;
import com.example.demo.repositories.commands.ProductCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final ProductCommandRepository productCommandRepository;

    /**
     * Upload image synchronously (for immediate return)
     * Returns a temporary placeholder URL
     */
    public String getPlaceholderUrl() {
        return "https://via.placeholder.com/800x800?text=Uploading...";
    }

    /**
     * Upload image asynchronously and update product
     * @param productId Product ID to update
     * @param file Image file to upload
     */
    @Async("taskExecutor")
    @Transactional(transactionManager = "writeTransactionManager")
    public CompletableFuture<String> uploadImageAsync(Long productId, MultipartFile file) {
        try {
            log.info("🔄 Starting async image upload for product: {}", productId);

            // Store file bytes before async operation
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();

            // Validate file
            validateImageFile(file);

            // Generate unique public_id
            String publicId = "products/" + UUID.randomUUID().toString();

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "wine-shop/products",
                            "resource_type", "image",
                            "transformation", new com.cloudinary.Transformation()
                                    .width(800).height(800).crop("limit").quality("auto")
                    ));

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Image uploaded successfully: {}", imageUrl);

            // Update product with real image URL
            Product product = productCommandRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            // Delete placeholder if exists
            if (product.getImages() != null && product.getImages().contains("placeholder")) {
                product.setImages(imageUrl);
            } else {
                product.setImages(imageUrl);
            }

            productCommandRepository.save(product);
            log.info("✅ Product {} updated with image URL", productId);

            return CompletableFuture.completedFuture(imageUrl);

        } catch (IOException e) {
            log.error("❌ Failed to upload image for product {}", productId, e);

            // Update product with error status or keep placeholder
            try {
                Product product = productCommandRepository.findById(productId).orElse(null);
                if (product != null) {
                    product.setImages("https://via.placeholder.com/800x800?text=Upload+Failed");
                    productCommandRepository.save(product);
                }
            } catch (Exception ex) {
                log.error("Failed to update product with error image", ex);
            }

            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Upload image synchronously (blocking)
     */
    public String uploadImage(MultipartFile file) {
        try {
            validateImageFile(file);

            String publicId = "products/" + UUID.randomUUID().toString();

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "wine-shop/products",
                            "resource_type", "image",
                            "transformation", new com.cloudinary.Transformation()
                                    .width(800).height(800).crop("limit").quality("auto")
                    ));

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Image uploaded successfully: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            log.error("❌ Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Delete image from Cloudinary
     */
    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty() || imageUrl.contains("placeholder")) {
                return;
            }

            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("✅ Image deleted successfully: {}", publicId);
            }

        } catch (Exception e) {
            log.error("❌ Failed to delete image from Cloudinary: {}", imageUrl, e);
        }
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     */
    private String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String path = parts[1];
            path = path.replaceFirst("v\\d+/", "");
            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex > 0) {
                path = path.substring(0, lastDotIndex);
            }

            return path;
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", imageUrl);
            return null;
        }
    }
}