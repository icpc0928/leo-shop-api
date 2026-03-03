package com.leoshop.service.crypto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResult {
    private boolean success;
    private String message;
    private BigDecimal actualAmount;
}
