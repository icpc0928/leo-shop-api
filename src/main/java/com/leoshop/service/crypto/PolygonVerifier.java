package com.leoshop.service.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolygonVerifier implements ChainVerifier {

    private final RestTemplate restTemplate;

    @Value("${etherscan.api-key:}")
    private String apiKey;

    @Override
    public String getNetwork() {
        return "polygon";
    }

    @Override
    public VerifyResult verify(String txHash, String expectedWallet, String contractAddress, BigDecimal expectedAmount) {
        try {
            // Etherscan API V2: chainid=137 for Polygon
            String url = "https://api.etherscan.io/v2/api?chainid=137"
                    + "&module=proxy&action=eth_getTransactionReceipt&txhash=" + txHash;
            if (apiKey != null && !apiKey.isBlank()) {
                url += "&apikey=" + apiKey;
            }

            log.info("Polygon verify URL: {}", url);
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response == null || response.get("result") == null || response.get("result").isNull()) {
                return VerifyResult.builder().success(false).message("Transaction not found").build();
            }

            JsonNode result = response.get("result");

            // Check if it's an error message string
            if (result.isTextual()) {
                return VerifyResult.builder().success(false).message("API error: " + result.asText()).build();
            }

            String status = result.has("status") ? result.get("status").asText() : "";
            if (!"0x1".equals(status)) {
                return VerifyResult.builder().success(false).message("Transaction failed on chain").build();
            }

            // For ERC-20 tokens, check logs for Transfer event
            if (contractAddress != null && !contractAddress.isBlank()) {
                JsonNode logs = result.get("logs");
                if (logs != null && logs.isArray()) {
                    for (JsonNode logEntry : logs) {
                        String logAddress = logEntry.get("address").asText();
                        if (logAddress.equalsIgnoreCase(contractAddress)) {
                            JsonNode topics = logEntry.get("topics");
                            if (topics != null && topics.size() >= 3) {
                                String to = "0x" + topics.get(2).asText().substring(26);
                                if (to.equalsIgnoreCase(expectedWallet)) {
                                    String data = logEntry.get("data").asText();
                                    BigDecimal amount = new BigDecimal(new java.math.BigInteger(data.substring(2), 16))
                                            .divide(BigDecimal.TEN.pow(18), 8, RoundingMode.HALF_UP);
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
                }
                return VerifyResult.builder().success(false).message("No matching transfer log found").build();
            }

            // Native token (POL/MATIC)
            String to = result.has("to") ? result.get("to").asText() : "";
            if (!to.equalsIgnoreCase(expectedWallet)) {
                return VerifyResult.builder().success(false).message("Recipient mismatch").build();
            }

            return VerifyResult.builder().success(true).message("Verified (native)").build();
        } catch (Exception e) {
            log.error("Polygon verification failed: {}", e.getMessage());
            return VerifyResult.builder().success(false).message("Verification error: " + e.getMessage()).build();
        }
    }
}
