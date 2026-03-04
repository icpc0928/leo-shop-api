package com.leoshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.leoshop.model.PaymentMethod;
import com.leoshop.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoRateService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final RestTemplate restTemplate;

    private static final Map<String, String> SYMBOL_TO_COINGECKO_ID = Map.of(
        "BTC", "bitcoin",
        "ETH", "ethereum",
        "USDT", "tether",
        "USDC", "usd-coin",
        "BNB", "binancecoin",
        "SOL", "solana",
        "XRP", "ripple",
        "ADA", "cardano",
        "DOGE", "dogecoin",
        "MATIC", "matic-network"
    );

    @Transactional
    public void refreshApiRates() {
        List<PaymentMethod> apiMethods = paymentMethodRepository
            .findAll().stream()
            .filter(m -> "api".equalsIgnoreCase(m.getRateSource()))
            .toList();

        if (apiMethods.isEmpty()) {
            log.info("No payment methods with rateSource=api, skipping refresh");
            return;
        }

        // Collect unique CoinGecko IDs
        Map<String, String> idToSymbol = new HashMap<>();
        for (PaymentMethod m : apiMethods) {
            String id = SYMBOL_TO_COINGECKO_ID.get(m.getSymbol().toUpperCase());
            if (id != null) {
                idToSymbol.put(id, m.getSymbol());
            }
        }

        if (idToSymbol.isEmpty()) {
            log.warn("No supported symbols found for API rate update");
            return;
        }

        try {
            String ids = String.join(",", idToSymbol.keySet());
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=twd";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null) {
                log.error("CoinGecko API returned null");
                return;
            }

            int updated = 0;
            for (Map.Entry<String, String> entry : idToSymbol.entrySet()) {
                String id = entry.getKey();
                String symbol = entry.getValue();
                JsonNode priceNode = response.get(id);
                if (priceNode != null && priceNode.has("twd")) {
                    BigDecimal rate = BigDecimal.valueOf(priceNode.get("twd").asDouble());
                    
                    // Update all payment methods with this symbol and rateSource=api
                    for (PaymentMethod m : apiMethods) {
                        if (m.getSymbol().equalsIgnoreCase(symbol)) {
                            m.setExchangeRate(rate);
                            paymentMethodRepository.save(m);
                            updated++;
                            log.info("Updated {} rate to {} TWD", symbol, rate);
                        }
                    }
                }
            }

            log.info("Crypto rates refresh completed, {} rates updated", updated);
        } catch (Exception e) {
            log.error("Failed to refresh crypto rates from CoinGecko: {}", e.getMessage());
        }
    }
}
