package com.rentalpro.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rentalpro.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired(required = false)
    private Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        if (cloudinary == null) {
            throw new Exception("File upload service is not configured. Please set up Cloudinary credentials.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Validate file type (images and PDFs only)
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only images and PDF files are allowed");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String publicId = folder + "/" + UUID.randomUUID().toString() + extension;

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "resource_type", "auto"
                )
            );

            String url = (String) uploadResult.get("secure_url");
            log.info("File uploaded successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new Exception("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String publicId) throws Exception {
        if (cloudinary == null) {
            throw new Exception("File upload service is not configured. Please set up Cloudinary credentials.");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting file from Cloudinary", e);
            throw new Exception("Failed to delete file: " + e.getMessage());
        }
    }
}
