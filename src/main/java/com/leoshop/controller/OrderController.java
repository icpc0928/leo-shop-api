package com.leoshop.controller;

import com.leoshop.dto.*;
import com.leoshop.model.User;
import com.leoshop.repository.UserRepository;
import com.leoshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication auth, @RequestBody CreateOrderRequest request) {
        Long userId = getUserId(auth);
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping
    public ResponseEntity<OrderListResponse> myOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersByUser(getUserId(auth), page, size));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(Authentication auth, @PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, getUserId(auth)));
    }

    @GetMapping("/shipping-fee")
    public ResponseEntity<Map<String, Object>> getShippingFee(@RequestParam BigDecimal subtotal) {
        BigDecimal freeThreshold = BigDecimal.valueOf(2000);
        BigDecimal fee = subtotal.compareTo(freeThreshold) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        return ResponseEntity.ok(Map.of(
                "shippingFee", fee,
                "freeThreshold", freeThreshold,
                "subtotal", subtotal,
                "total", subtotal.add(fee)
        ));
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getId();
    }
}
