package com.leoshop.dto;

import lombok.Data;

@Data
public class CryptoPaymentRequest {
    private Long orderId;
    private String payCurrency;
}
