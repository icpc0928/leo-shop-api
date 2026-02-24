package com.leoshop.service;

import com.leoshop.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileUploadService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Path.of(uploadDir));
    }

    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BadRequestException("File too large: " + file.getOriginalFilename());
            }
            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new BadRequestException("Unsupported file type: " + file.getContentType());
            }

            String original = file.getOriginalFilename();
            String sanitized = (original != null) ? original.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
            String filename = System.currentTimeMillis() + "_" + new Random().nextInt(10000) + "_" + sanitized;

            try {
                Path target = Path.of(uploadDir, filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                urls.add("/" + uploadDir + "/" + filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }
        return urls;
    }

    public void deleteFile(String url) {
        // url like /uploads/products/xxx.jpg
        String relativePath = url.startsWith("/") ? url.substring(1) : url;
        Path filePath = Path.of(relativePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
