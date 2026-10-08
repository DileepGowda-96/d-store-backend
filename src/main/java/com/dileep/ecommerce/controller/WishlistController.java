package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.WishlistResponse;
import com.dileep.ecommerce.service.AuthService;
import com.dileep.ecommerce.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    public WishlistController(
            WishlistService wishlistService,
            AuthService authService) {

        this.wishlistService = wishlistService;
        this.authService = authService;
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                wishlistService.addToWishlist(
                        userId,
                        productId
                )
        );
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                wishlistService.getWishlist(userId)
        );
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                wishlistService.removeFromWishlist(
                        userId,
                        productId
                )
        );
    }

    private Long getLoggedInUserId(
            Authentication authentication) {

        String email = authentication.getName();

        return authService
                .getUserByEmail(email)
                .getId();
    }
}