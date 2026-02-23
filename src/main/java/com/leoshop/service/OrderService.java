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

        BigDecimal shippingFee = totalAmount.compareTo(BigDecimal.valueOf(2000)) >= 0
                ? BigDecimal.ZERO : BigDecimal.valueOf(100);
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

    public OrderListResponse getAllOrders(String status, int page, int size) {
        Page<Order> orders;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatus(Order.OrderStatus.valueOf(status), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return toListResponse(orders, page);
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
