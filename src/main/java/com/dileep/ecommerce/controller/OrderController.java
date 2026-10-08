package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.OrderResponse;
import com.dileep.ecommerce.entity.Order;
import com.dileep.ecommerce.entity.OrderStatus;
import com.dileep.ecommerce.service.AuthService;
import com.dileep.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(
            OrderService orderService,
            AuthService authService) {

        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                orderService.placeOrder(userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                orderService.getUserOrders(userId)
        );
    }

    @GetMapping("/{orderId}/details")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        OrderResponse order =
                orderService.getOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You can only view your own order"
            );
        }

        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                orderService.cancelOrder(userId, orderId)
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        orderId,
                        status
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