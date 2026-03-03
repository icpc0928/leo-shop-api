package com.leoshop.service.crypto;

import com.leoshop.model.CryptoOrder;
import com.leoshop.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CryptoVerifyService {

    private final Map<String, ChainVerifier> verifiers;

    public CryptoVerifyService(List<ChainVerifier> verifierList) {
        this.verifiers = verifierList.stream()
                .collect(Collectors.toMap(ChainVerifier::getNetwork, Function.identity()));
        log.info("Registered chain verifiers: {}", verifiers.keySet());
    }

    public VerifyResult verify(CryptoOrder order, PaymentMethod method) {
        ChainVerifier verifier = verifiers.get(method.getNetwork());
        if (verifier == null) {
            return VerifyResult.builder()
                    .success(false)
                    .message("No verifier available for network: " + method.getNetwork())
                    .build();
        }
        return verifier.verify(order.getTxHash(), method.getWalletAddress(),
                method.getContractAddress(), order.getExpectedAmount());
    }
}
