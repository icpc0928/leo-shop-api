package com.leoshop.dto;

import com.leoshop.model.Product;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private String imageUrl;
    private List<String> imageUrls;
    private String category;
    private Integer stock;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product p) {
        List<String> images;
        if (p.getImageUrls() != null && !p.getImageUrls().isBlank()) {
            images = Arrays.asList(p.getImageUrls().split(","));
        } else if (p.getImageUrl() != null) {
            images = List.of(p.getImageUrl());
        } else {
            images = List.of();
        }

        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .description(p.getDescription()).price(p.getPrice())
                .comparePrice(p.getComparePrice()).imageUrl(p.getImageUrl())
                .imageUrls(images)
                .category(p.getCategory()).stock(p.getStock()).active(p.getActive())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
