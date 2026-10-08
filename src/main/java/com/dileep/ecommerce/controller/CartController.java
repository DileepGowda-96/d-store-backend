package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.AddToCartRequest;
import com.dileep.ecommerce.dto.CartResponse;
import com.dileep.ecommerce.dto.UpdateCartItemRequest;
import com.dileep.ecommerce.service.AuthService;
import com.dileep.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    public CartController(
            CartService cartService,
            AuthService authService) {

        this.cartService = cartService;
        this.authService = authService;
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                cartService.addToCart(userId, request)
        );
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        userId,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                cartService.removeCartItem(
                        userId,
                        itemId
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