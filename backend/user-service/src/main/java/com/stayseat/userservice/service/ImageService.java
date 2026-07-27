package com.stayseat.userservice.service;

import com.stayseat.userservice.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Stores profile images on the local filesystem and returns a public URL.
 * Local stand-in for Amazon S3 (report 5.5) - swapping to S3 means changing
 * only this class.
 */
@Service
public class ImageService {

    private final Path uploadDir;
    private final String baseUrl;

    public ImageService(@Value("${app.upload.dir}") String uploadDir,
                        @Value("${app.public-base-url}") String baseUrl) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String store(MultipartFile file, UUID userId) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Image file is required.");
        }
        String extension = extensionOf(file.getOriginalFilename());
        String filename = userId + (extension.isEmpty() ? "" : "." + extension);
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(uploadDir.resolve(filename));
        } catch (IOException e) {
            throw ApiException.imageUploadFailed();
        }
        return baseUrl + "/uploads/" + filename;
    }

    private String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
