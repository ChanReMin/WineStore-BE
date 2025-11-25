package com.example.demo.services.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {

    /**
     * Upload single image to Cloudinary
     * @param file Image file
     * @param folder Folder name in Cloudinary
     * @return Upload result containing URL and public_id
     * @throws IOException if upload fails
     */
    Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException;

}
