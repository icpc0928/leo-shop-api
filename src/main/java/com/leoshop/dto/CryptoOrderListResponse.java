package com.leoshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoOrderListResponse {
    private List<CryptoOrderResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
