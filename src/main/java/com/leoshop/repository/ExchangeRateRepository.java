package com.leoshop.repository;

import com.leoshop.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCurrency(String currency);
    Optional<ExchangeRate> findByBaseCurrency(Boolean baseCurrency);
}
