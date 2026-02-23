package com.leoshop.repository;

import com.leoshop.model.Order;
import com.leoshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o")
    long getOrderCount();

    @Query("SELECT CAST(o.createdAt AS date) as date, SUM(o.totalAmount) as revenue FROM Order o WHERE o.status != 'CANCELLED' AND o.createdAt >= :since GROUP BY CAST(o.createdAt AS date) ORDER BY date")
    List<Object[]> getDailyRevenue(LocalDateTime since);
}
