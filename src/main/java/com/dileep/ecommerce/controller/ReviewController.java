package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.ReviewRequest;
import com.dileep.ecommerce.dto.ReviewResponse;
import com.dileep.ecommerce.service.AuthService;
import com.dileep.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    public ReviewController(
            ReviewService reviewService,
            AuthService authService) {

        this.reviewService = reviewService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                reviewService.createReview(
                        userId,
                        request
                )
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                reviewService.getReviewsByProduct(productId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                reviewService.getReviewsByUser(userId)
        );
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable Long reviewId) {

        return ResponseEntity.ok(
                reviewService.getReviewById(reviewId)
        );
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        return ResponseEntity.ok(
                reviewService.updateReview(
                        userId,
                        reviewId,
                        request
                )
        );
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        Long userId = getLoggedInUserId(authentication);

        reviewService.deleteReview(
                userId,
                reviewId
        );

        return ResponseEntity.ok(
                "Review deleted successfully"
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