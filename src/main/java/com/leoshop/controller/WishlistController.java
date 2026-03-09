package com.leoshop.controller;

import com.leoshop.dto.ProductResponse;
import com.leoshop.model.User;
import com.leoshop.repository.UserRepository;
import com.leoshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getWishlist(Authentication auth) {
        Long userId = getUserId(auth);
        List<ProductResponse> products = wishlistService.getWishlistProducts(userId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
        Authentication auth,
        @PathVariable Long productId
    ) {
        Long userId = getUserId(auth);
        boolean added = wishlistService.toggle(userId, productId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "wishlisted", added,
            "message", added ? "Added to wishlist" : "Removed from wishlist"
        ));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
        Authentication auth,
        @PathVariable Long productId
    ) {
        Long userId = getUserId(auth);
        boolean wishlisted = wishlistService.isWishlisted(userId, productId);
        return ResponseEntity.ok(Map.of("wishlisted", wishlisted));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getWishlistIds(Authentication auth) {
        Long userId = getUserId(auth);
        List<Long> productIds = wishlistService.getWishlistProductIds(userId);
        return ResponseEntity.ok(productIds);
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
