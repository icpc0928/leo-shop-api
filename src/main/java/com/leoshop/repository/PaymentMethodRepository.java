package com.leoshop.repository;

import com.leoshop.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByEnabledTrueOrderBySortOrder();
    Optional<PaymentMethod> findBySymbol(String symbol);
}
