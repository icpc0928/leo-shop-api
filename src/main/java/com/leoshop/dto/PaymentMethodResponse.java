package com.leoshop.dto;

import com.leoshop.model.PaymentMethod;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private String name;
    private String symbol;
    private String network;
    private String contractAddress;
    private String walletAddress;
    private BigDecimal exchangeRate;
    private String rateSource;
    private String gateway;
    private String explorerUrl;
    private String apiEndpoint;
    private String iconUrl;
    private Boolean enabled;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentMethodResponse from(PaymentMethod m) {
        return PaymentMethodResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .symbol(m.getSymbol())
                .network(m.getNetwork())
                .contractAddress(m.getContractAddress())
                .walletAddress(m.getWalletAddress())
                .exchangeRate(m.getExchangeRate())
                .rateSource(m.getRateSource())
                .gateway(m.getGateway())
                .explorerUrl(m.getExplorerUrl())
                .apiEndpoint(m.getApiEndpoint())
                .iconUrl(m.getIconUrl())
                .enabled(m.getEnabled())
                .sortOrder(m.getSortOrder())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
