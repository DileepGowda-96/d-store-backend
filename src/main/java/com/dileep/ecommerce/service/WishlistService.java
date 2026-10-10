package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.WishlistProductResponse;
import com.dileep.ecommerce.dto.WishlistResponse;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.entity.Wishlist;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.ProductRepository;
import com.dileep.ecommerce.repository.UserRepository;
import com.dileep.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long productId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .user(user)
                            .build();

                    return wishlistRepository.save(newWishlist);
                });

        boolean alreadyExists = wishlist.getProducts().stream()
                .anyMatch(existing ->
                        existing.getId().equals(productId));

        if (alreadyExists) {
            throw new RuntimeException(
                    "Product already exists in wishlist");
        }

        wishlist.addProduct(product);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        return convertToResponse(savedWishlist);
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wishlist not found"));

        return convertToResponse(wishlist);
    }

    @Transactional
    public WishlistResponse removeFromWishlist(Long userId, Long productId) {

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wishlist not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        boolean exists = wishlist.getProducts().stream()
                .anyMatch(existing ->
                        existing.getId().equals(productId));

        if (!exists) {
            throw new RuntimeException(
                    "Product is not in wishlist");
        }

        wishlist.removeProduct(product);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        return convertToResponse(savedWishlist);
    }

    private WishlistResponse convertToResponse(Wishlist wishlist) {

        List<WishlistProductResponse> products =
                wishlist.getProducts()
                        .stream()
                        .map(this::convertProductToResponse)
                        .toList();

        return new WishlistResponse(
                wishlist.getId(),
                wishlist.getUser().getId(),
                products
        );
    }

    private WishlistProductResponse convertProductToResponse(
            Product product) {

        return new WishlistProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl()
        );
    }
}