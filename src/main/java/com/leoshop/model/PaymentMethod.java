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
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String network;

    private String contractAddress;

    private String walletAddress;

    @Column(precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    @Builder.Default
    private String rateSource = "manual";

    @Column(nullable = false)
    @Builder.Default
    private String gateway = "direct";

    private String explorerUrl;

    private String apiEndpoint;

    private String apiKey;

    private String iconUrl;

    @Builder.Default
    private Boolean enabled = false;

    @Builder.Default
    private Integer sortOrder = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
