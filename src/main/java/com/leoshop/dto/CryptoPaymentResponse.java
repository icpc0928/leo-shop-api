package com.leoshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CryptoPaymentResponse {
    private String paymentId;
    private String payAddress;
    private String payAmount;
    private String payCurrency;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private String status;
}
