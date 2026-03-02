package com.leoshop.controller;

import com.leoshop.dto.CryptoPaymentRequest;
import com.leoshop.dto.CryptoPaymentResponse;
import com.leoshop.service.NowPaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/crypto")
@RequiredArgsConstructor
public class CryptoPaymentController {

    private final NowPaymentsService nowPaymentsService;

    @PostMapping("/create")
    public ResponseEntity<CryptoPaymentResponse> createPayment(@RequestBody CryptoPaymentRequest request) {
        return ResponseEntity.ok(nowPaymentsService.createPayment(request.getOrderId(), request.getPayCurrency()));
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<CryptoPaymentResponse> getStatus(@PathVariable String paymentId) {
        return ResponseEntity.ok(nowPaymentsService.getPaymentStatus(paymentId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader("x-nowpayments-sig") String signature,
            @RequestBody String body) {
        nowPaymentsService.handleWebhook(body, signature);
        return ResponseEntity.ok("OK");
    }
}
