package com.leoshop.controller;

import com.leoshop.dto.ExchangeRatesResponse;
import com.leoshop.model.ExchangeRate;
import com.leoshop.service.ExchangeRateService;
import com.leoshop.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService service;
    private final SystemSettingsService systemSettingsService;

    @GetMapping
    public ExchangeRatesResponse getAllRates() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (ExchangeRate er : service.getAllRates()) {
            map.put(er.getCurrency(), er.getRate());
        }
        String baseCurrency = systemSettingsService.getSetting("base_currency", "TWD");
        return ExchangeRatesResponse.builder()
                .baseCurrency(baseCurrency)
                .rates(map)
                .build();
    }
}
