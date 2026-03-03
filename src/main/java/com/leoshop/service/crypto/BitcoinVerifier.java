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
public class BitcoinVerifier implements ChainVerifier {

    private final RestTemplate restTemplate;

    @Override
    public String getNetwork() {
        return "bitcoin";
    }

    @Override
    public VerifyResult verify(String txHash, String expectedWallet, String contractAddress, BigDecimal expectedAmount) {
        try {
            String url = "https://blockchain.info/rawtx/" + txHash;
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null) {
                return VerifyResult.builder().success(false).message("Transaction not found").build();
            }

            JsonNode outputs = response.get("out");
            if (outputs != null && outputs.isArray()) {
                for (JsonNode output : outputs) {
                    String addr = output.has("addr") ? output.get("addr").asText() : "";
                    if (addr.equalsIgnoreCase(expectedWallet)) {
                        long satoshis = output.get("value").asLong();
                        BigDecimal amount = BigDecimal.valueOf(satoshis)
                                .divide(BigDecimal.valueOf(100_000_000), 8, RoundingMode.HALF_UP);
                        if (amount.compareTo(expectedAmount) >= 0) {
                            return VerifyResult.builder().success(true).message("Verified").actualAmount(amount).build();
                        }
                        return VerifyResult.builder().success(false)
                                .message("Amount mismatch: expected " + expectedAmount + ", got " + amount)
                                .actualAmount(amount).build();
                    }
                }
            }

            return VerifyResult.builder().success(false).message("No output to expected wallet").build();
        } catch (Exception e) {
            log.error("Bitcoin verification failed: {}", e.getMessage());
            return VerifyResult.builder().success(false).message("Verification error: " + e.getMessage()).build();
        }
    }
}
