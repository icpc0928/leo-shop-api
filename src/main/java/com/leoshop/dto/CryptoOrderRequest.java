package com.leoshop.dto;

import lombok.Data;

@Data
public class CryptoOrderRequest {
    private Long orderId;
    private Long paymentMethodId;
}
