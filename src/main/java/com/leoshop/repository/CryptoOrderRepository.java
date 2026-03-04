package com.leoshop.repository;

import com.leoshop.model.CryptoOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoOrderRepository extends JpaRepository<CryptoOrder, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<CryptoOrder> {
    Optional<CryptoOrder> findByOrderId(Long orderId);
    Optional<CryptoOrder> findByTxHash(String txHash);
}
