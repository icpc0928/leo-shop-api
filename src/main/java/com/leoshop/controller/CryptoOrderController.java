package com.leoshop.controller;

import com.leoshop.dto.CryptoOrderRequest;
import com.leoshop.dto.CryptoOrderResponse;
import com.leoshop.service.CryptoOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crypto-orders")
@RequiredArgsConstructor
public class CryptoOrderController {

    private final CryptoOrderService cryptoOrderService;

    @PostMapping
    public ResponseEntity<CryptoOrderResponse> create(@RequestBody CryptoOrderRequest request) {
        return ResponseEntity.ok(cryptoOrderService.createOrder(request.getOrderId(), request.getPaymentMethodId()));
    }

    @PutMapping("/{id}/submit-hash")
    public ResponseEntity<CryptoOrderResponse> submitHash(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(cryptoOrderService.submitHash(id, body.get("txHash")));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<CryptoOrderResponse> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(cryptoOrderService.getStatus(id));
    }
}
