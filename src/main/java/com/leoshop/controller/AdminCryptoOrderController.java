package com.leoshop.controller;

import com.leoshop.dto.CryptoOrderResponse;
import com.leoshop.service.CryptoOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/crypto-orders")
@RequiredArgsConstructor
public class AdminCryptoOrderController {

    private final CryptoOrderService cryptoOrderService;

    @GetMapping
    public ResponseEntity<List<CryptoOrderResponse>> getAll() {
        return ResponseEntity.ok(cryptoOrderService.getAll());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<CryptoOrderResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(cryptoOrderService.verify(id));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<CryptoOrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(cryptoOrderService.manualConfirm(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CryptoOrderResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(cryptoOrderService.manualReject(id));
    }
}
