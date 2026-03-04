package com.leoshop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemSettingsRequest {
    private String baseCurrency;
    private BigDecimal shippingFee;
    private BigDecimal freeShippingThreshold;
}
