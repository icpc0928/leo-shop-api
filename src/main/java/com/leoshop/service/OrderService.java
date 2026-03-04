package com.leoshop.service;

import com.leoshop.dto.*;
import com.leoshop.exception.BadRequestException;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.*;
import com.leoshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final SystemSettingsService systemSettingsService;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .shippingName(request.getShippingName())
                .shippingPhone(request.getShippingPhone())
                .shippingEmail(request.getShippingEmail())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            productService.decreaseStock(product.getId(), itemReq.getQuantity());

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();
            order.getItems().add(item);
            totalAmount = totalAmount.add(subtotal);
        }

        BigDecimal freeThreshold = systemSettingsService.getSettingAsDecimal("free_shipping_threshold", "2000");
        BigDecimal shippingFeeAmount = systemSettingsService.getSettingAsDecimal("shipping_fee", "100");
        BigDecimal shippingFee = totalAmount.compareTo(freeThreshold) >= 0
                ? BigDecimal.ZERO : shippingFeeAmount;
        order.setTotalAmount(totalAmount.add(shippingFee));
        order.setShippingFee(shippingFee);

        return OrderResponse.from(orderRepository.save(order));
    }

    public OrderListResponse getOrdersByUser(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Order> orders = orderRepository.findByUser(user, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return toListResponse(orders, page);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        return OrderResponse.from(orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found")));
    }

    public OrderListResponse getAllOrders(String status, String orderNumber, String startDate, String endDate, int page, int size) {
        Page<Order> orders;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Build specification for filtering
        if (orderNumber != null || startDate != null || endDate != null || status != null) {
            orders = orderRepository.findAll(buildOrderSpecification(status, orderNumber, startDate, endDate), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return toListResponse(orders, page);
    }
    
    private org.springframework.data.jpa.domain.Specification<Order> buildOrderSpecification(
            String status, String orderNumber, String startDate, String endDate) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), Order.OrderStatus.valueOf(status)));
            }
            if (orderNumber != null && !orderNumber.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("orderNumber")), "%" + orderNumber.toLowerCase() + "%"));
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
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(Order.OrderStatus.valueOf(status));
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your order");
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        // Restore stock
        for (OrderItem item : order.getItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    public Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "LS-" + date + "-" + random;
    }

    private OrderListResponse toListResponse(Page<Order> orders, int page) {
        return OrderListResponse.builder()
                .content(orders.getContent().stream().map(OrderResponse::from).toList())
                .totalPages(orders.getTotalPages())
                .totalElements(orders.getTotalElements())
                .currentPage(page)
                .build();
    }
}
