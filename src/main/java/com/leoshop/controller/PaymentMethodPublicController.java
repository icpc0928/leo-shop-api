package com.leoshop.controller;

import com.leoshop.dto.PaymentMethodResponse;
import com.leoshop.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodPublicController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> getEnabled() {
        return ResponseEntity.ok(paymentMethodService.getEnabled());
    }
}
