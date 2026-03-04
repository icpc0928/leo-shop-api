package com.leoshop.service;

import com.leoshop.dto.SystemSettingsRequest;
import com.leoshop.dto.SystemSettingsResponse;
import com.leoshop.exception.BadRequestException;
import com.leoshop.model.ExchangeRate;
import com.leoshop.model.PaymentMethod;
import com.leoshop.model.SystemSettings;
import com.leoshop.repository.ExchangeRateRepository;
import com.leoshop.repository.PaymentMethodRepository;
import com.leoshop.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @PostConstruct
    public void init() {
        // Initialize default settings if not exists
        if (systemSettingsRepository.findBySettingKey("base_currency").isEmpty()) {
            systemSettingsRepository.save(SystemSettings.builder()
                    .settingKey("base_currency")
                    .settingValue("TWD")
                    .build());
            log.info("Initialized default base_currency: TWD");
        }
        if (systemSettingsRepository.findBySettingKey("shipping_fee").isEmpty()) {
            systemSettingsRepository.save(SystemSettings.builder()
                    .settingKey("shipping_fee")
                    .settingValue("100")
                    .build());
            log.info("Initialized default shipping_fee: 100");
        }
        if (systemSettingsRepository.findBySettingKey("free_shipping_threshold").isEmpty()) {
            systemSettingsRepository.save(SystemSettings.builder()
                    .settingKey("free_shipping_threshold")
                    .settingValue("2000")
                    .build());
            log.info("Initialized default free_shipping_threshold: 2000");
        }

        // Ensure at least one currency is marked as base if none exists
        Optional<ExchangeRate> baseCurrency = exchangeRateRepository.findByBaseCurrency(true);
        if (baseCurrency.isEmpty()) {
            Optional<ExchangeRate> twd = exchangeRateRepository.findByCurrency("TWD");
            if (twd.isPresent()) {
                twd.get().setBaseCurrency(true);
                twd.get().setRate(BigDecimal.ONE);
                exchangeRateRepository.save(twd.get());
                log.info("Set TWD as base currency");
            }
        }
    }

    public SystemSettingsResponse getSettings() {
        String baseCurrency = getSetting("base_currency", "TWD");
        BigDecimal shippingFee = new BigDecimal(getSetting("shipping_fee", "100"));
        BigDecimal freeShippingThreshold = new BigDecimal(getSetting("free_shipping_threshold", "2000"));
        List<String> availableCurrencies = exchangeRateRepository.findAll()
                .stream()
                .map(ExchangeRate::getCurrency)
                .toList();

        return SystemSettingsResponse.builder()
                .baseCurrency(baseCurrency)
                .shippingFee(shippingFee)
                .freeShippingThreshold(freeShippingThreshold)
                .availableCurrencies(availableCurrencies)
                .build();
    }

    @Transactional
    public SystemSettingsResponse updateSettings(SystemSettingsRequest request) {
        String oldBaseCurrency = getSetting("base_currency", "TWD");

        // Update base currency if changed
        if (request.getBaseCurrency() != null && !request.getBaseCurrency().equals(oldBaseCurrency)) {
            updateBaseCurrency(oldBaseCurrency, request.getBaseCurrency());
        }

        // Update shipping settings
        if (request.getShippingFee() != null) {
            updateSetting("shipping_fee", request.getShippingFee().toString());
        }
        if (request.getFreeShippingThreshold() != null) {
            updateSetting("free_shipping_threshold", request.getFreeShippingThreshold().toString());
        }

        return getSettings();
    }

    @Transactional
    protected void updateBaseCurrency(String oldCurrency, String newCurrency) {
        // Find new base currency
        ExchangeRate newBase = exchangeRateRepository.findByCurrency(newCurrency)
                .orElseThrow(() -> new BadRequestException("Currency not found: " + newCurrency));

        // Get old base currency rate (for recalculation)
        BigDecimal oldBaseRate = newBase.getRate();

        // Clear all base currency flags
        List<ExchangeRate> allRates = exchangeRateRepository.findAll();
        for (ExchangeRate rate : allRates) {
            rate.setBaseCurrency(false);
        }

        // Set new base currency
        newBase.setBaseCurrency(true);
        newBase.setRate(BigDecimal.ONE);

        // Recalculate all rates relative to new base
        // Formula: newRate = oldRate / oldBaseRate
        for (ExchangeRate rate : allRates) {
            if (!rate.getCurrency().equals(newCurrency)) {
                BigDecimal newRate = rate.getRate().divide(oldBaseRate, 10, RoundingMode.HALF_UP);
                rate.setRate(newRate);
            }
        }

        exchangeRateRepository.saveAll(allRates);

        // Recalculate payment methods' exchange rates
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        for (PaymentMethod pm : paymentMethods) {
            if (pm.getExchangeRate() != null && pm.getExchangeRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newRate = pm.getExchangeRate().divide(oldBaseRate, 10, RoundingMode.HALF_UP);
                pm.setExchangeRate(newRate);
            }
        }
        paymentMethodRepository.saveAll(paymentMethods);

        updateSetting("base_currency", newCurrency);

        log.info("Updated base currency from {} to {}, recalculated {} exchange rates and {} payment methods", 
                oldCurrency, newCurrency, allRates.size(), paymentMethods.size());
    }

    public String getSetting(String key, String defaultValue) {
        return systemSettingsRepository.findBySettingKey(key)
                .map(SystemSettings::getSettingValue)
                .orElse(defaultValue);
    }

    public BigDecimal getSettingAsDecimal(String key, String defaultValue) {
        return new BigDecimal(getSetting(key, defaultValue));
    }

    private void updateSetting(String key, String value) {
        Optional<SystemSettings> existing = systemSettingsRepository.findBySettingKey(key);
        if (existing.isPresent()) {
            existing.get().setSettingValue(value);
            systemSettingsRepository.save(existing.get());
        } else {
            systemSettingsRepository.save(SystemSettings.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .build());
        }
    }
}
