package com.leoshop.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProductListResponse {
    private List<ProductResponse> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
}
