package com.leoshop.controller;

import com.leoshop.model.SitePage;
import com.leoshop.service.SitePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SitePageController {

    private final SitePageService sitePageService;

    // Public
    @GetMapping("/api/pages/{slug}")
    public ResponseEntity<SitePage> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(sitePageService.getBySlug(slug));
    }

    // Admin
    @GetMapping("/api/admin/pages")
    public ResponseEntity<List<SitePage>> getAllPages() {
        return ResponseEntity.ok(sitePageService.getAllPages());
    }

    @PostMapping("/api/admin/pages")
    public ResponseEntity<SitePage> create(@RequestBody SitePage page) {
        return ResponseEntity.ok(sitePageService.create(page));
    }

    @PutMapping("/api/admin/pages/{id}")
    public ResponseEntity<SitePage> update(@PathVariable Long id, @RequestBody SitePage page) {
        return ResponseEntity.ok(sitePageService.update(id, page));
    }

    @DeleteMapping("/api/admin/pages/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sitePageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/pages/{id}/toggle")
    public ResponseEntity<SitePage> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(sitePageService.toggle(id));
    }
}
