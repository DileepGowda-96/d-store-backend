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

    public WishlistResponse addToWishlist(
            Long userId,
            Long productId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseGet(() -> {

                    Wishlist newWishlist = Wishlist.builder()
                            .user(user)
                            .build();

                    return wishlistRepository.save(newWishlist);
                });

        if (wishlist.getProducts().contains(product)) {
            throw new RuntimeException(
                    "Product already exists in wishlist"
            );
        }

        wishlist.getProducts().add(product);

        return convertToResponse(
                wishlistRepository.save(wishlist)
        );
    }

    public WishlistResponse getWishlist(Long userId) {

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wishlist not found"
                        ));

        return convertToResponse(wishlist);
    }

    public WishlistResponse removeFromWishlist(
            Long userId,
            Long productId) {

        Wishlist wishlist = wishlistRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wishlist not found"
                        ));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        ));

        if (!wishlist.getProducts().contains(product)) {
            throw new RuntimeException(
                    "Product is not in wishlist"
            );
        }

        wishlist.getProducts().remove(product);

        return convertToResponse(
                wishlistRepository.save(wishlist)
        );
    }

    private WishlistResponse convertToResponse(
            Wishlist wishlist) {

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