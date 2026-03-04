package com.leoshop.controller;

import com.leoshop.dto.SystemSettingsRequest;
import com.leoshop.dto.SystemSettingsResponse;
import com.leoshop.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SystemSettingsResponse> updateSettings(@RequestBody SystemSettingsRequest request) {
        return ResponseEntity.ok(systemSettingsService.updateSettings(request));
    }
}
