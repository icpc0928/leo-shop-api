package com.leoshop.controller;

import com.leoshop.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<Map<String, List<String>>> upload(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = fileUploadService.uploadFiles(files);
        return ResponseEntity.ok(Map.of("urls", urls));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody Map<String, String> body) {
        fileUploadService.deleteFile(body.get("url"));
        return ResponseEntity.noContent().build();
    }
}
