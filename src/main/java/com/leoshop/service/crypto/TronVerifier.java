package com.leoshop.service.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class TronVerifier implements ChainVerifier {

    private final RestTemplate restTemplate;

    private static final String BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    @Override
    public String getNetwork() {
        return "tron";
    }

    @Override
    public VerifyResult verify(String txHash, String expectedWallet, String contractAddress, BigDecimal expectedAmount) {
        try {
            // Convert Base58 wallet address to hex for comparison
            String expectedHex = base58ToHex(expectedWallet);
            log.info("Tron verify: expectedWallet={}, expectedHex={}", expectedWallet, expectedHex);

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
                        
                        // Compare: API returns hex (0x...), we convert Base58 to hex
                        boolean addressMatch = to.equalsIgnoreCase(expectedWallet) 
                            || to.equalsIgnoreCase(expectedHex)
                            || normalizeHex(to).equalsIgnoreCase(normalizeHex(expectedHex));
                        
                        if (addressMatch) {
                            String valueStr = result.has("value") ? result.get("value").asText() : "0";
                            BigDecimal amount = new BigDecimal(valueStr)
                                    .divide(BigDecimal.TEN.pow(6), 8, RoundingMode.HALF_UP);
                            if (amount.compareTo(expectedAmount) == 0) {
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

    /**
     * Convert Tron Base58 address (T...) to hex format (without 0x prefix).
     * Tron addresses: 41 prefix (hex) = T prefix (Base58)
     */
    private String base58ToHex(String base58) {
        if (base58 == null || base58.isEmpty()) return "";
        
        // If already hex format
        if (base58.startsWith("0x") || base58.startsWith("0X")) return base58;
        if (!base58.startsWith("T")) return base58;

        try {
            byte[] decoded = base58Decode(base58);
            if (decoded.length < 5) return base58;
            
            // Remove the 41 prefix and 4-byte checksum
            // Tron address = 41 + 20bytes address + 4bytes checksum
            // We want just the 20 bytes (without 41 prefix)
            byte[] addressBytes = Arrays.copyOfRange(decoded, 1, 21);
            return bytesToHex(addressBytes);
        } catch (Exception e) {
            log.warn("Failed to convert Base58 address: {}", e.getMessage());
            return base58;
        }
    }

    private byte[] base58Decode(String input) {
        BigInteger bi = BigInteger.ZERO;
        for (int i = 0; i < input.length(); i++) {
            int index = BASE58_ALPHABET.indexOf(input.charAt(i));
            if (index < 0) throw new IllegalArgumentException("Invalid Base58 char: " + input.charAt(i));
            bi = bi.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(index));
        }
        
        byte[] bytes = bi.toByteArray();
        // Count leading zeros
        int leadingZeros = 0;
        for (int i = 0; i < input.length() && input.charAt(i) == '1'; i++) {
            leadingZeros++;
        }
        
        // Strip sign byte if present
        int stripSignByte = (bytes.length > 1 && bytes[0] == 0) ? 1 : 0;
        byte[] result = new byte[leadingZeros + bytes.length - stripSignByte];
        System.arraycopy(bytes, stripSignByte, result, leadingZeros, bytes.length - stripSignByte);
        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Normalize hex: remove 0x prefix, lowercase */
    private String normalizeHex(String hex) {
        if (hex == null) return "";
        String h = hex.toLowerCase();
        if (h.startsWith("0x")) h = h.substring(2);
        return h;
    }
}
