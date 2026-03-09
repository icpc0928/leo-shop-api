package com.leoshop.controller;

import com.leoshop.dto.SystemSettingsRequest;
import com.leoshop.dto.SystemSettingsResponse;
import com.leoshop.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping("/api/site-info")
    public ResponseEntity<?> getSiteInfo() {
        var settings = systemSettingsService.getSettings();
        return ResponseEntity.ok(java.util.Map.of(
            "siteName", settings.getSiteName(),
            "baseCurrency", settings.getBaseCurrency()
        ));
    }

    @GetMapping("/api/admin/settings")
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping("/api/admin/settings")
    public ResponseEntity<SystemSettingsResponse> updateSettings(@RequestBody SystemSettingsRequest request) {
        return ResponseEntity.ok(systemSettingsService.updateSettings(request));
    }
}
