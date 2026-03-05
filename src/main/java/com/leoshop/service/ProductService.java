package com.leoshop.service;

import com.leoshop.dto.*;
import com.leoshop.exception.BadRequestException;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.Product;
import com.leoshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductListResponse getAllProducts(String category, String keyword, String sort, int page, int size) {
        return getAllProducts(category, keyword, sort, page, size, false);
    }

    public ProductListResponse getAllProducts(String category, String keyword, String sort, int page, int size, boolean includeInactive) {
        Sort sortOrder = Sort.by("createdAt").descending();
        if (sort != null) {
            sortOrder = switch (sort) {
                case "price_asc" -> Sort.by("price").ascending();
                case "price_desc" -> Sort.by("price").descending();
                case "name_asc" -> Sort.by("name").ascending();
                case "name_desc" -> Sort.by("name").descending();
                case "newest" -> Sort.by("createdAt").descending();
                default -> sortOrder;
            };
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Product> products;
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (includeInactive) {
            // Admin: show all products
            if (hasCategory && hasKeyword) {
                products = productRepository.findByCategoryAndNameContaining(category, keyword, pageable);
            } else if (hasCategory) {
                products = productRepository.findByCategory(category, pageable);
            } else if (hasKeyword) {
                products = productRepository.findByNameContaining(keyword, pageable);
            } else {
                products = productRepository.findAll(pageable);
            }
        } else {
            // Public: only active products
            if (hasCategory && hasKeyword) {
                products = productRepository.findByActiveTrueAndCategoryAndNameContaining(category, keyword, pageable);
            } else if (hasCategory) {
                products = productRepository.findByActiveTrueAndCategory(category, pageable);
            } else if (hasKeyword) {
                products = productRepository.findByActiveTrueAndNameContaining(keyword, pageable);
            } else {
                products = productRepository.findByActiveTrue(pageable);
            }
        }

        return ProductListResponse.builder()
                .content(products.getContent().stream().map(ProductResponse::from).toList())
                .totalPages(products.getTotalPages())
                .totalElements(products.getTotalElements())
                .currentPage(page)
                .build();
    }

    public ProductResponse getProductBySlug(String slug) {
        return ProductResponse.from(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    public ProductResponse getProductById(Long id) {
        return ProductResponse.from(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapRequest(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        mapRequest(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    @Transactional
    public void increaseStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    private void mapRequest(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setSlug(req.getSlug());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setComparePrice(req.getComparePrice());
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            product.setImageUrls(String.join(",", req.getImageUrls()));
            product.setImageUrl(req.getImageUrls().get(0));
        } else if (req.getImageUrl() != null) {
            product.setImageUrl(req.getImageUrl());
        }
        product.setCategory(req.getCategory());
        product.setStock(req.getStock());
        if (req.getActive() != null) product.setActive(req.getActive());
    }
}
