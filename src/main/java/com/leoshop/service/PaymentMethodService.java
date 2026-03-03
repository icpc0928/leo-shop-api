package com.leoshop.service;

import com.leoshop.dto.PaymentMethodRequest;
import com.leoshop.dto.PaymentMethodResponse;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.PaymentMethod;
import com.leoshop.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethodResponse> getAll() {
        return paymentMethodRepository.findAll().stream()
                .map(PaymentMethodResponse::from).toList();
    }

    public List<PaymentMethodResponse> getEnabled() {
        return paymentMethodRepository.findByEnabledTrueOrderBySortOrder().stream()
                .map(PaymentMethodResponse::from).toList();
    }

    public PaymentMethodResponse getById(Long id) {
        return PaymentMethodResponse.from(findById(id));
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest req) {
        PaymentMethod m = PaymentMethod.builder()
                .name(req.getName())
                .symbol(req.getSymbol())
                .network(req.getNetwork())
                .contractAddress(req.getContractAddress())
                .walletAddress(req.getWalletAddress())
                .exchangeRate(req.getExchangeRate())
                .rateSource(req.getRateSource() != null ? req.getRateSource() : "manual")
                .gateway(req.getGateway() != null ? req.getGateway() : "direct")
                .explorerUrl(req.getExplorerUrl())
                .apiEndpoint(req.getApiEndpoint())
                .apiKey(req.getApiKey())
                .iconUrl(req.getIconUrl())
                .enabled(req.getEnabled() != null ? req.getEnabled() : false)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        return PaymentMethodResponse.from(paymentMethodRepository.save(m));
    }

    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest req) {
        PaymentMethod m = findById(id);
        if (req.getName() != null) m.setName(req.getName());
        if (req.getSymbol() != null) m.setSymbol(req.getSymbol());
        if (req.getNetwork() != null) m.setNetwork(req.getNetwork());
        if (req.getContractAddress() != null) m.setContractAddress(req.getContractAddress());
        if (req.getWalletAddress() != null) m.setWalletAddress(req.getWalletAddress());
        if (req.getExchangeRate() != null) m.setExchangeRate(req.getExchangeRate());
        if (req.getRateSource() != null) m.setRateSource(req.getRateSource());
        if (req.getGateway() != null) m.setGateway(req.getGateway());
        if (req.getExplorerUrl() != null) m.setExplorerUrl(req.getExplorerUrl());
        if (req.getApiEndpoint() != null) m.setApiEndpoint(req.getApiEndpoint());
        if (req.getApiKey() != null) m.setApiKey(req.getApiKey());
        if (req.getIconUrl() != null) m.setIconUrl(req.getIconUrl());
        if (req.getEnabled() != null) m.setEnabled(req.getEnabled());
        if (req.getSortOrder() != null) m.setSortOrder(req.getSortOrder());
        return PaymentMethodResponse.from(paymentMethodRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        PaymentMethod m = findById(id);
        paymentMethodRepository.delete(m);
    }

    @Transactional
    public PaymentMethodResponse toggle(Long id) {
        PaymentMethod m = findById(id);
        m.setEnabled(!m.getEnabled());
        return PaymentMethodResponse.from(paymentMethodRepository.save(m));
    }

    public PaymentMethod findById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod not found"));
    }
}
