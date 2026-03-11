package com.leoshop.controller;

import com.leoshop.model.Banner;
import com.leoshop.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // Public
    @GetMapping("/api/banners")
    public ResponseEntity<List<Banner>> getPublicBanners() {
        return ResponseEntity.ok(bannerService.getPublicBanners());
    }

    // Admin
    @GetMapping("/api/admin/banners")
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @PostMapping("/api/admin/banners")
    public ResponseEntity<Banner> create(@RequestBody Banner banner) {
        return ResponseEntity.ok(bannerService.create(banner));
    }

    @PutMapping("/api/admin/banners/{id}")
    public ResponseEntity<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        return ResponseEntity.ok(bannerService.update(id, banner));
    }

    @DeleteMapping("/api/admin/banners/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/banners/{id}/toggle")
    public ResponseEntity<Banner> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.toggle(id));
    }
}
