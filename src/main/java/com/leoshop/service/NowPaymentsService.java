package com.leoshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leoshop.dto.CryptoPaymentResponse;
import com.leoshop.exception.BadRequestException;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.CryptoPayment;
import com.leoshop.model.Order;
import com.leoshop.repository.CryptoPaymentRepository;
import com.leoshop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NowPaymentsService {

    private final CryptoPaymentRepository cryptoPaymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${nowpayments.api-key}")
    private String apiKey;

    @Value("${nowpayments.ipn-secret}")
    private String ipnSecret;

    @Value("${nowpayments.base-url}")
    private String baseUrl;

    @Transactional
    public CryptoPaymentResponse createPayment(Long orderId, String payCurrency) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        Map<String, Object> body = Map.of(
                "price_amount", order.getTotalAmount(),
                "price_currency", "twd",
                "pay_currency", payCurrency,
                "order_id", "order_" + orderId,
                "order_description", "Leo Shop Order #" + order.getOrderNumber()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/payment", request, JsonNode.class);

        JsonNode data = response.getBody();
        if (data == null) {
            throw new BadRequestException("Empty response from NOWPayments");
        }

        CryptoPayment payment = CryptoPayment.builder()
                .orderId(orderId)
                .nowPaymentId(data.get("payment_id").asText())
                .payAddress(data.has("pay_address") ? data.get("pay_address").asText() : null)
                .payAmount(data.has("pay_amount") ? data.get("pay_amount").asText() : null)
                .payCurrency(payCurrency)
                .priceAmount(order.getTotalAmount())
                .priceCurrency("twd")
                .status(data.has("payment_status") ? data.get("payment_status").asText() : "waiting")
                .build();

        cryptoPaymentRepository.save(payment);

        order.setCryptoPaymentId(payment.getNowPaymentId());
        order.setPaymentMethod("CRYPTO");
        orderRepository.save(order);

        return CryptoPaymentResponse.builder()
                .paymentId(payment.getNowPaymentId())
                .payAddress(payment.getPayAddress())
                .payAmount(payment.getPayAmount())
                .payCurrency(payment.getPayCurrency())
                .priceAmount(payment.getPriceAmount())
                .priceCurrency(payment.getPriceCurrency())
                .status(payment.getStatus())
                .build();
    }

    public CryptoPaymentResponse getPaymentStatus(String paymentId) {
        CryptoPayment payment = cryptoPaymentRepository.findByNowPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Also query NOWPayments for latest status
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    baseUrl + "/payment/" + paymentId, HttpMethod.GET, request, JsonNode.class);
            JsonNode data = response.getBody();
            if (data != null && data.has("payment_status")) {
                String newStatus = data.get("payment_status").asText();
                if (!newStatus.equals(payment.getStatus())) {
                    payment.setStatus(newStatus);
                    cryptoPaymentRepository.save(payment);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to query NOWPayments status for {}: {}", paymentId, e.getMessage());
        }

        return CryptoPaymentResponse.builder()
                .paymentId(payment.getNowPaymentId())
                .payAddress(payment.getPayAddress())
                .payAmount(payment.getPayAmount())
                .payCurrency(payment.getPayCurrency())
                .priceAmount(payment.getPriceAmount())
                .priceCurrency(payment.getPriceCurrency())
                .status(payment.getStatus())
                .build();
    }

    @Transactional
    public void handleWebhook(String body, String signature) {
        if (!verifySignature(body, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        try {
            JsonNode data = objectMapper.readTree(body);
            String paymentStatus = data.get("payment_status").asText();
            String paymentId = data.get("payment_id").asText();

            CryptoPayment payment = cryptoPaymentRepository.findByNowPaymentId(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

            payment.setStatus(paymentStatus);
            cryptoPaymentRepository.save(payment);

            if ("finished".equals(paymentStatus) || "confirmed".equals(paymentStatus)) {
                Order order = orderRepository.findById(payment.getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                order.setStatus(Order.OrderStatus.PAID);
                orderRepository.save(order);
                log.info("Order {} marked as PAID via crypto payment {}", order.getId(), paymentId);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to process webhook");
        }
    }

    private boolean verifySignature(String body, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(
                    ipnSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
