package com.leoshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsResponse {
    private String baseCurrency;
    private BigDecimal shippingFee;
    private BigDecimal freeShippingThreshold;
    private List<String> availableCurrencies;
}
