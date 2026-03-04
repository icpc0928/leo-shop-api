package com.leoshop.config;

import com.leoshop.service.CryptoRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledConfig {

    private final CryptoRateService cryptoRateService;

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void refreshCryptoRates() {
        log.info("Scheduled crypto rate refresh triggered");
        cryptoRateService.refreshApiRates();
    }
}
