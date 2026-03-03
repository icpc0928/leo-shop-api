package com.leoshop.service;

import com.leoshop.dto.CryptoOrderResponse;
import com.leoshop.exception.BadRequestException;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.CryptoOrder;
import com.leoshop.model.Order;
import com.leoshop.model.PaymentMethod;
import com.leoshop.repository.CryptoOrderRepository;
import com.leoshop.repository.OrderRepository;
import com.leoshop.service.crypto.CryptoVerifyService;
import com.leoshop.service.crypto.VerifyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoOrderService {

    private final CryptoOrderRepository cryptoOrderRepository;
    private final PaymentMethodService paymentMethodService;
    private final OrderRepository orderRepository;
    private final CryptoVerifyService cryptoVerifyService;

    @Transactional
    public CryptoOrderResponse createOrder(Long orderId, Long paymentMethodId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        PaymentMethod method = paymentMethodService.findById(paymentMethodId);

        if (!method.getEnabled()) {
            throw new BadRequestException("Payment method is not enabled");
        }
        if (method.getExchangeRate() == null || method.getExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Exchange rate not configured");
        }
        if (method.getWalletAddress() == null || method.getWalletAddress().isBlank()) {
            throw new BadRequestException("Wallet address not configured");
        }

        // Calculate amount: TWD / exchangeRate + random identifier
        BigDecimal baseAmount = order.getTotalAmount()
                .divide(method.getExchangeRate(), 8, RoundingMode.HALF_UP);
        double randomId = ThreadLocalRandom.current().nextDouble(0.0001, 0.0099);
        BigDecimal expectedAmount = baseAmount.add(BigDecimal.valueOf(randomId))
                .setScale(4, RoundingMode.HALF_UP);

        CryptoOrder cryptoOrder = CryptoOrder.builder()
                .orderId(orderId)
                .paymentMethodId(paymentMethodId)
                .symbol(method.getSymbol())
                .network(method.getNetwork())
                .expectedAmount(expectedAmount)
                .walletAddress(method.getWalletAddress())
                .verifyStatus("pending")
                .build();

        cryptoOrder = cryptoOrderRepository.save(cryptoOrder);

        order.setPaymentMethod("CRYPTO_DIRECT");
        order.setCryptoPaymentId(String.valueOf(cryptoOrder.getId()));
        orderRepository.save(order);

        return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
    }

    @Transactional
    public CryptoOrderResponse submitHash(Long cryptoOrderId, String txHash) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);

        if (!"pending".equals(cryptoOrder.getVerifyStatus())) {
            throw new BadRequestException("Order is not in pending status");
        }

        cryptoOrder.setTxHash(txHash);
        cryptoOrder.setPaidAt(LocalDateTime.now());
        cryptoOrderRepository.save(cryptoOrder);

        // Try auto-verify
        try {
            PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
            VerifyResult result = cryptoVerifyService.verify(cryptoOrder, method);
            if (result.isSuccess()) {
                cryptoOrder.setVerifyStatus("verified");
                cryptoOrder.setActualAmount(result.getActualAmount());
                cryptoOrder.setVerifiedAt(LocalDateTime.now());
                // Update order status
                Order order = orderRepository.findById(cryptoOrder.getOrderId())
                        .orElse(null);
                if (order != null) {
                    order.setStatus(Order.OrderStatus.PAID);
                    orderRepository.save(order);
                }
            } else {
                cryptoOrder.setVerifyStatus("failed");
                cryptoOrder.setVerifyMessage(result.getMessage());
            }
            cryptoOrderRepository.save(cryptoOrder);
            return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
        } catch (Exception e) {
            log.warn("Auto-verify failed for crypto order {}: {}", cryptoOrderId, e.getMessage());
            // Leave as pending, admin can manually verify
            PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
            return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
        }
    }

    public CryptoOrderResponse getStatus(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
    }

    public List<CryptoOrderResponse> getAll() {
        return cryptoOrderRepository.findAll().stream()
                .map(o -> {
                    PaymentMethod m = paymentMethodService.findById(o.getPaymentMethodId());
                    return CryptoOrderResponse.from(o, m.getExplorerUrl());
                }).toList();
    }

    @Transactional
    public CryptoOrderResponse verify(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());

        if (cryptoOrder.getTxHash() == null || cryptoOrder.getTxHash().isBlank()) {
            throw new BadRequestException("No transaction hash submitted");
        }

        VerifyResult result = cryptoVerifyService.verify(cryptoOrder, method);
        if (result.isSuccess()) {
            cryptoOrder.setVerifyStatus("verified");
            cryptoOrder.setActualAmount(result.getActualAmount());
            cryptoOrder.setVerifiedAt(LocalDateTime.now());
            Order order = orderRepository.findById(cryptoOrder.getOrderId()).orElse(null);
            if (order != null) {
                order.setStatus(Order.OrderStatus.PAID);
                orderRepository.save(order);
            }
        } else {
            cryptoOrder.setVerifyStatus("failed");
            cryptoOrder.setVerifyMessage(result.getMessage());
        }
        cryptoOrderRepository.save(cryptoOrder);
        return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
    }

    @Transactional
    public CryptoOrderResponse manualConfirm(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        cryptoOrder.setVerifyStatus("manual");
        cryptoOrder.setVerifyMessage("Manually confirmed by admin");
        cryptoOrder.setVerifiedAt(LocalDateTime.now());
        cryptoOrderRepository.save(cryptoOrder);

        Order order = orderRepository.findById(cryptoOrder.getOrderId()).orElse(null);
        if (order != null) {
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
        }

        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
    }

    @Transactional
    public CryptoOrderResponse manualReject(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        cryptoOrder.setVerifyStatus("failed");
        cryptoOrder.setVerifyMessage("Rejected by admin");
        cryptoOrder.setVerifiedAt(LocalDateTime.now());
        cryptoOrderRepository.save(cryptoOrder);

        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method.getExplorerUrl());
    }

    private CryptoOrder findEntity(Long id) {
        return cryptoOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CryptoOrder not found"));
    }
}
