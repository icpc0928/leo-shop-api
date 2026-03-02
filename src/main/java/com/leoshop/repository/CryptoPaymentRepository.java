package com.leoshop.repository;

import com.leoshop.model.CryptoPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoPaymentRepository extends JpaRepository<CryptoPayment, Long> {
    Optional<CryptoPayment> findByNowPaymentId(String nowPaymentId);
    Optional<CryptoPayment> findByOrderId(Long orderId);
}
