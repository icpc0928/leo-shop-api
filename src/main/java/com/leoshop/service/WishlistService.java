package com.leoshop.service;

import com.leoshop.dto.ProductResponse;
import com.leoshop.model.Product;
import com.leoshop.model.Wishlist;
import com.leoshop.repository.ProductRepository;
import com.leoshop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional
    public boolean toggle(Long userId, Long productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }

        var existing = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            // Remove from wishlist
            wishlistRepository.delete(existing.get());
            return false; // removed
        } else {
            // Add to wishlist
            Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .productId(productId)
                .build();
            wishlistRepository.save(wishlist);
            return true; // added
        }
    }

    public List<ProductResponse> getWishlistProducts(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        List<Long> productIds = wishlists.stream()
            .map(Wishlist::getProductId)
            .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findAllById(productIds);
        return products.stream()
            .map(ProductResponse::from)
            .collect(Collectors.toList());
    }

    public boolean isWishlisted(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    public List<Long> getWishlistProductIds(Long userId) {
        return wishlistRepository.findProductIdsByUserId(userId);
    }

}
