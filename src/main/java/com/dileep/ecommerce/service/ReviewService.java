package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.ReviewRequest;
import com.dileep.ecommerce.dto.ReviewResponse;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.entity.Review;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.ProductRepository;
import com.dileep.ecommerce.repository.ReviewRepository;
import com.dileep.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public ReviewResponse createReview(
            Long userId,
            ReviewRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Product not found"
                ));

        if (reviewRepository
                .findByUserIdAndProductId(
                        userId,
                        request.getProductId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "You have already reviewed this product"
            );
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewDate(LocalDateTime.now())
                .build();

        return convertToResponse(
                reviewRepository.save(review)
        );
    }

    public List<ReviewResponse> getReviewsByProduct(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(
                    "Product not found"
            );
        }

        return reviewRepository
                .findByProductId(productId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ReviewResponse> getReviewsByUser(
            Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found"
            );
        }

        return reviewRepository
                .findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ReviewResponse getReviewById(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found"
                        ));

        return convertToResponse(review);
    }

    public ReviewResponse updateReview(
            Long userId,
            Long reviewId,
            ReviewRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found"
                        ));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You can only update your own review"
            );
        }

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Product not found"
                ));

        if (!review.getProduct().getId()
                .equals(request.getProductId())) {

            throw new RuntimeException(
                    "Product cannot be changed"
            );
        }

        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return convertToResponse(
                reviewRepository.save(review)
        );
    }

    public void deleteReview(
            Long userId,
            Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Review not found"
                        ));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You can only delete your own review"
            );
        }

        reviewRepository.delete(review);
    }

    private ReviewResponse convertToResponse(
            Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate()
        );
    }
}