package com.leoshop.controller;

import com.leoshop.dto.PaymentMethodRequest;
import com.leoshop.dto.PaymentMethodResponse;
import com.leoshop.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> getAll() {
        return ResponseEntity.ok(paymentMethodService.getAll());
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(@RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> update(@PathVariable Long id, @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PaymentMethodResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.toggle(id));
    }
}
