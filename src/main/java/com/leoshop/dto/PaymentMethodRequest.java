package com.leoshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentMethodRequest {
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
    private String apiKey;
    private String iconUrl;
    private Boolean enabled;
    private Integer sortOrder;
}
