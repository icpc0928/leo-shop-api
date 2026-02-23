package com.leoshop.controller;

import com.leoshop.repository.OrderRepository;
import com.leoshop.repository.ProductRepository;
import com.leoshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRevenue", orderRepository.getTotalRevenue());
        stats.put("totalOrders", orderRepository.getOrderCount());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalUsers", userRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<Map<String, Object>>> getRevenue() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> raw = orderRepository.getDailyRevenue(since);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", row[0].toString());
            entry.put("revenue", row[1] != null ? row[1] : BigDecimal.ZERO);
            result.add(entry);
        }
        return ResponseEntity.ok(result);
    }
}
