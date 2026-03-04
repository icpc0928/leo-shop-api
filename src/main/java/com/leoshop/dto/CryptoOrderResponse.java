package com.leoshop.dto;

import com.leoshop.model.CryptoOrder;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CryptoOrderResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long paymentMethodId;
    private String symbol;
    private String network;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String walletAddress;
    private String txHash;
    private String verifyStatus;
    private String verifyMessage;
    private String contractAddress;
    private String explorerUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;

    public static CryptoOrderResponse from(CryptoOrder o, com.leoshop.model.PaymentMethod m) {
        return from(o, m.getExplorerUrl(), null, m.getContractAddress());
    }

    public static CryptoOrderResponse from(CryptoOrder o, String explorerUrl) {
        return from(o, explorerUrl, null, null);
    }

    public static CryptoOrderResponse from(CryptoOrder o, String explorerUrl, String orderNumber) {
        return from(o, explorerUrl, orderNumber, null);
    }

    public static CryptoOrderResponse from(CryptoOrder o, String explorerUrl, String orderNumber, String contractAddress) {
        return CryptoOrderResponse.builder()
                .id(o.getId())
                .orderId(o.getOrderId())
                .orderNumber(orderNumber)
                .paymentMethodId(o.getPaymentMethodId())
                .symbol(o.getSymbol())
                .network(o.getNetwork())
                .expectedAmount(o.getExpectedAmount())
                .actualAmount(o.getActualAmount())
                .walletAddress(o.getWalletAddress())
                .txHash(o.getTxHash())
                .verifyStatus(o.getVerifyStatus())
                .verifyMessage(o.getVerifyMessage())
                .contractAddress(contractAddress)
                .explorerUrl(explorerUrl)
                .createdAt(o.getCreatedAt())
                .paidAt(o.getPaidAt())
                .verifiedAt(o.getVerifiedAt())
                .build();
    }
}
