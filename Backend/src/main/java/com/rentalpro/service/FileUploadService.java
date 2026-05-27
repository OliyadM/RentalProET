package com.rentalpro.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadFile(MultipartFile file, String folder) throws Exception;
    void deleteFile(String publicId) throws Exception;
}
