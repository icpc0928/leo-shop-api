package com.leoshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crypto_orders")
public class CryptoOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long paymentMethodId;

    private String symbol;

    private String network;

    @Column(precision = 20, scale = 8)
    private BigDecimal expectedAmount;

    @Column(precision = 20, scale = 8)
    private BigDecimal actualAmount;

    private String walletAddress;

    private String txHash;

    @Builder.Default
    private String verifyStatus = "pending";

    @Column(columnDefinition = "TEXT")
    private String verifyMessage;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
