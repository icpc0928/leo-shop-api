package com.leoshop.controller;

import com.leoshop.model.ExchangeRate;
import com.leoshop.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/exchange-rates")
@RequiredArgsConstructor
public class AdminExchangeRateController {

    private final ExchangeRateService service;

    @GetMapping
    public List<ExchangeRate> getAllRates() {
        return service.getAllRates();
    }

    @PutMapping("/{currency}")
    public ExchangeRate updateRate(@PathVariable String currency, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal rate = body.get("rate");
        if (rate == null) throw new IllegalArgumentException("rate is required");
        return service.updateRate(currency, rate);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshRates() {
        service.refreshRates();
        return ResponseEntity.ok(Map.of("message", "Exchange rates refreshed"));
    }
}
