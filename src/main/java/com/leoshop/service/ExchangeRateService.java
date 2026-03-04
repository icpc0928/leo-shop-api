package com.leoshop.service;

import com.leoshop.model.ExchangeRate;
import com.leoshop.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository repository;

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "JPY", "EUR", "GBP", "CNY", "KRW", "THB", "VND", "SGD", "HKD"
    );

    public List<ExchangeRate> getAllRates() {
        return repository.findAll();
    }

    public ExchangeRate updateRate(String currency, BigDecimal rate) {
        ExchangeRate er = repository.findByCurrency(currency.toUpperCase())
            .orElseGet(() -> ExchangeRate.builder().currency(currency.toUpperCase()).build());
        er.setRate(rate);
        er.setUpdatedAt(LocalDateTime.now());
        return repository.save(er);
    }

    @SuppressWarnings("unchecked")
    public void refreshRates() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(
                "https://open.er-api.com/v6/latest/TWD", Map.class);
            if (response == null || !"success".equals(response.get("result"))) {
                log.warn("Failed to fetch exchange rates");
                return;
            }
            Map<String, Number> rates = (Map<String, Number>) response.get("rates");
            if (rates == null) return;

            for (String currency : SUPPORTED_CURRENCIES) {
                Number rateValue = rates.get(currency);
                if (rateValue != null) {
                    updateRate(currency, BigDecimal.valueOf(rateValue.doubleValue()));
                }
            }
            log.info("Exchange rates refreshed from API");
        } catch (Exception e) {
            log.error("Error refreshing exchange rates: {}", e.getMessage());
        }
    }
}
