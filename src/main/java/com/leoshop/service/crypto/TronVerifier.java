package com.leoshop.service.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class TronVerifier implements ChainVerifier {

    private final RestTemplate restTemplate;

    @Override
    public String getNetwork() {
        return "tron";
    }

    @Override
    public VerifyResult verify(String txHash, String expectedWallet, String contractAddress, BigDecimal expectedAmount) {
        try {
            String url = "https://api.trongrid.io/v1/transactions/" + txHash + "/events";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || !response.has("data") || !response.get("data").isArray()) {
                return VerifyResult.builder().success(false).message("Transaction not found").build();
            }

            JsonNode events = response.get("data");
            for (JsonNode event : events) {
                String eventName = event.has("event_name") ? event.get("event_name").asText() : "";
                if ("Transfer".equals(eventName)) {
                    JsonNode result = event.get("result");
                    if (result != null) {
                        String to = result.has("to") ? result.get("to").asText() : "";
                        if (to.equalsIgnoreCase(expectedWallet)) {
                            String valueStr = result.has("value") ? result.get("value").asText() : "0";
                            BigDecimal amount = new BigDecimal(valueStr)
                                    .divide(BigDecimal.TEN.pow(6), 8, RoundingMode.HALF_UP);
                            if (amount.compareTo(expectedAmount) >= 0) {
                                return VerifyResult.builder().success(true).message("Verified").actualAmount(amount).build();
                            }
                            return VerifyResult.builder().success(false)
                                    .message("Amount mismatch: expected " + expectedAmount + ", got " + amount)
                                    .actualAmount(amount).build();
                        }
                    }
                }
            }

            return VerifyResult.builder().success(false).message("No matching transfer event found").build();
        } catch (Exception e) {
            log.error("Tron verification failed: {}", e.getMessage());
            return VerifyResult.builder().success(false).message("Verification error: " + e.getMessage()).build();
        }
    }
}
