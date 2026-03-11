package com.leoshop.controller;

import com.leoshop.model.Faq;
import com.leoshop.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // Public
    @GetMapping("/api/faqs")
    public ResponseEntity<List<Faq>> getPublicFaqs() {
        return ResponseEntity.ok(faqService.getPublicFaqs());
    }

    // Admin
    @GetMapping("/api/admin/faqs")
    public ResponseEntity<List<Faq>> getAllFaqs() {
        return ResponseEntity.ok(faqService.getAllFaqs());
    }

    @PostMapping("/api/admin/faqs")
    public ResponseEntity<Faq> create(@RequestBody Faq faq) {
        return ResponseEntity.ok(faqService.create(faq));
    }

    @PutMapping("/api/admin/faqs/{id}")
    public ResponseEntity<Faq> update(@PathVariable Long id, @RequestBody Faq faq) {
        return ResponseEntity.ok(faqService.update(id, faq));
    }

    @DeleteMapping("/api/admin/faqs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        faqService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/faqs/{id}/toggle")
    public ResponseEntity<Faq> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.toggle(id));
    }
}
