package com.leoshop.service.crypto;

import java.math.BigDecimal;

public interface ChainVerifier {
    String getNetwork();
    VerifyResult verify(String txHash, String expectedWallet, String contractAddress, BigDecimal expectedAmount);
}
