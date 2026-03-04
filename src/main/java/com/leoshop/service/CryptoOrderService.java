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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CryptoOrderService {

    private final CryptoOrderRepository cryptoOrderRepository;
    private final PaymentMethodService paymentMethodService;
    private final OrderRepository orderRepository;
    private final CryptoVerifyService cryptoVerifyService;

    // Cache: key = "symbol:amount", value = true. Expires after 20 minutes.
    private final Cache<String, Boolean> usedAmountCache = Caffeine.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    public CryptoOrderService(CryptoOrderRepository cryptoOrderRepository,
                               PaymentMethodService paymentMethodService,
                               OrderRepository orderRepository,
                               CryptoVerifyService cryptoVerifyService) {
        this.cryptoOrderRepository = cryptoOrderRepository;
        this.paymentMethodService = paymentMethodService;
        this.orderRepository = orderRepository;
        this.cryptoVerifyService = cryptoVerifyService;
    }

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

        // Calculate amount: TWD / exchangeRate
        BigDecimal baseAmount = order.getTotalAmount()
                .divide(method.getExchangeRate(), 8, RoundingMode.HALF_UP);

        // Add random identifier: TWD 0.0001 ~ 0.9999 converted to crypto
        // On collision, increment by 0.0001 TWD until unique within 20-min cache window
        double randomTwd = 0.0001 + Math.random() * 0.9998;
        BigDecimal incrementStep = BigDecimal.valueOf(0.0001)
                .divide(method.getExchangeRate(), 8, RoundingMode.HALF_UP);
        BigDecimal randomCrypto = BigDecimal.valueOf(randomTwd)
                .divide(method.getExchangeRate(), 8, RoundingMode.HALF_UP);
        BigDecimal expectedAmount = baseAmount.add(randomCrypto);

        String cacheKey = method.getSymbol() + ":" + expectedAmount.toPlainString();
        while (usedAmountCache.getIfPresent(cacheKey) != null) {
            expectedAmount = expectedAmount.add(incrementStep);
            cacheKey = method.getSymbol() + ":" + expectedAmount.toPlainString();
        }
        usedAmountCache.put(cacheKey, Boolean.TRUE);

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

        return CryptoOrderResponse.from(cryptoOrder, method);
    }

    @Transactional
    public CryptoOrderResponse submitHash(Long cryptoOrderId, String txHash) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);

        if (!"pending".equals(cryptoOrder.getVerifyStatus())) {
            throw new BadRequestException("Order is not in pending status");
        }

        // Prevent duplicate txHash usage
        Optional<CryptoOrder> existing = cryptoOrderRepository.findByTxHash(txHash);
        if (existing.isPresent() && !existing.get().getId().equals(cryptoOrderId)) {
            throw new BadRequestException("This transaction hash has already been used for another order");
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
            return CryptoOrderResponse.from(cryptoOrder, method);
        } catch (Exception e) {
            log.warn("Auto-verify failed for crypto order {}: {}", cryptoOrderId, e.getMessage());
            // Leave as pending, admin can manually verify
            PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
            return CryptoOrderResponse.from(cryptoOrder, method);
        }
    }

    public CryptoOrderResponse getStatus(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method);
    }

    public com.leoshop.dto.CryptoOrderListResponse getAll(
            String status, String orderNumber, String txHash, 
            String startDate, String endDate, int page, int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, size, org.springframework.data.domain.Sort.by("id").descending());
        
        org.springframework.data.domain.Page<CryptoOrder> orders;
        
        if (status != null || orderNumber != null || txHash != null || startDate != null || endDate != null) {
            orders = cryptoOrderRepository.findAll(
                buildCryptoOrderSpecification(status, orderNumber, txHash, startDate, endDate), pageable);
        } else {
            orders = cryptoOrderRepository.findAll(pageable);
        }
        
        var content = orders.getContent().stream()
                .map(o -> {
                    PaymentMethod m = paymentMethodService.findById(o.getPaymentMethodId());
                    String orderNum = orderRepository.findById(o.getOrderId())
                            .map(Order::getOrderNumber).orElse(null);
                    return CryptoOrderResponse.from(o, m.getExplorerUrl(), orderNum, m.getContractAddress());
                }).toList();
        
        return com.leoshop.dto.CryptoOrderListResponse.builder()
                .content(content)
                .totalPages(orders.getTotalPages())
                .totalElements(orders.getTotalElements())
                .currentPage(page)
                .build();
    }
    
    private org.springframework.data.jpa.domain.Specification<CryptoOrder> buildCryptoOrderSpecification(
            String status, String orderNumber, String txHash, String startDate, String endDate) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("verifyStatus"), status));
            }
            if (orderNumber != null && !orderNumber.isEmpty()) {
                var subquery = query.subquery(Long.class);
                var subRoot = subquery.from(Order.class);
                subquery.select(subRoot.get("id"))
                        .where(cb.like(cb.lower(subRoot.get("orderNumber")), "%" + orderNumber.toLowerCase() + "%"));
                predicates.add(root.get("orderId").in(subquery));
            }
            if (txHash != null && !txHash.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("txHash")), "%" + txHash.toLowerCase() + "%"));
            }
            if (startDate != null && !startDate.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), 
                    java.time.LocalDate.parse(startDate).atStartOfDay()));
            }
            if (endDate != null && !endDate.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), 
                    java.time.LocalDate.parse(endDate).atTime(23, 59, 59)));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
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
        return CryptoOrderResponse.from(cryptoOrder, method);
    }

    @Transactional
    public CryptoOrderResponse manualConfirm(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        cryptoOrder.setVerifyStatus("verified");
        cryptoOrder.setVerifyMessage("Manually confirmed by admin");
        cryptoOrder.setVerifiedAt(LocalDateTime.now());
        cryptoOrderRepository.save(cryptoOrder);

        Order order = orderRepository.findById(cryptoOrder.getOrderId()).orElse(null);
        if (order != null) {
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
        }

        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method);
    }

    @Transactional
    public CryptoOrderResponse manualReject(Long cryptoOrderId) {
        CryptoOrder cryptoOrder = findEntity(cryptoOrderId);
        cryptoOrder.setVerifyStatus("failed");
        cryptoOrder.setVerifyMessage("Rejected by admin");
        cryptoOrder.setVerifiedAt(LocalDateTime.now());
        cryptoOrderRepository.save(cryptoOrder);

        PaymentMethod method = paymentMethodService.findById(cryptoOrder.getPaymentMethodId());
        return CryptoOrderResponse.from(cryptoOrder, method);
    }

    private CryptoOrder findEntity(Long id) {
        return cryptoOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CryptoOrder not found"));
    }
}
