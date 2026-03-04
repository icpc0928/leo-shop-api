package com.leoshop.dto;

import com.leoshop.model.Order;
import com.leoshop.model.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private String shippingName;
    private String shippingPhone;
    private String shippingEmail;
    private String shippingAddress;
    private String paymentMethod;
    private String cryptoPaymentId;
    private String note;
    private List<Item> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class Item {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal productPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber())
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount()).shippingFee(o.getShippingFee())
                .shippingName(o.getShippingName()).shippingPhone(o.getShippingPhone())
                .shippingEmail(o.getShippingEmail()).shippingAddress(o.getShippingAddress())
                .paymentMethod(o.getPaymentMethod()).cryptoPaymentId(o.getCryptoPaymentId()).note(o.getNote())
                .items(o.getItems().stream().map(OrderResponse::mapItem).toList())
                .createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt())
                .build();
    }

    private static Item mapItem(OrderItem i) {
        return Item.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .productPrice(i.getProductPrice())
                .quantity(i.getQuantity())
                .subtotal(i.getSubtotal())
                .build();
    }
}
