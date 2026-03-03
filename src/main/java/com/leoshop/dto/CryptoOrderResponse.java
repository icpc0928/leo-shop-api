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
    private Long paymentMethodId;
    private String symbol;
    private String network;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String walletAddress;
    private String txHash;
    private String verifyStatus;
    private String verifyMessage;
    private String explorerUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;

    public static CryptoOrderResponse from(CryptoOrder o, String explorerUrl) {
        return CryptoOrderResponse.builder()
                .id(o.getId())
                .orderId(o.getOrderId())
                .paymentMethodId(o.getPaymentMethodId())
                .symbol(o.getSymbol())
                .network(o.getNetwork())
                .expectedAmount(o.getExpectedAmount())
                .actualAmount(o.getActualAmount())
                .walletAddress(o.getWalletAddress())
                .txHash(o.getTxHash())
                .verifyStatus(o.getVerifyStatus())
                .verifyMessage(o.getVerifyMessage())
                .explorerUrl(explorerUrl)
                .createdAt(o.getCreatedAt())
                .paidAt(o.getPaidAt())
                .verifiedAt(o.getVerifiedAt())
                .build();
    }
}
