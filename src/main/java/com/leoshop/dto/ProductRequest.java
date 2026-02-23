package com.leoshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private String imageUrl;
    private String category;
    private Integer stock;
    private Boolean active = true;
}
