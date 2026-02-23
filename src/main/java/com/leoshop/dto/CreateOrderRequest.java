package com.leoshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private String shippingName;
    private String shippingPhone;
    private String shippingEmail;
    private String shippingAddress;
    private String paymentMethod;
    private String note;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
