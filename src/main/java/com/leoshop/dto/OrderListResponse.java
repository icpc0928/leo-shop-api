package com.leoshop.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderListResponse {
    private List<OrderResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
