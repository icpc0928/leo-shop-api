package com.leoshop.controller;

import com.leoshop.dto.CryptoOrderListResponse;
import com.leoshop.dto.CryptoOrderResponse;
import com.leoshop.service.CryptoOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/crypto-orders")
@RequiredArgsConstructor
public class AdminCryptoOrderController {

    private final CryptoOrderService cryptoOrderService;

    @GetMapping
    public ResponseEntity<CryptoOrderListResponse> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String txHash,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cryptoOrderService.getAll(status, orderNumber, txHash, startDate, endDate, page, size));
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
