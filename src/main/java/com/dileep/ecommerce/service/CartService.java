package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.AddToCartRequest;
import com.dileep.ecommerce.dto.CartItemResponse;
import com.dileep.ecommerce.dto.CartResponse;
import com.dileep.ecommerce.dto.UpdateCartItemRequest;
import com.dileep.ecommerce.entity.Cart;
import com.dileep.ecommerce.entity.CartItem;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.CartItemRepository;
import com.dileep.ecommerce.repository.CartRepository;
import com.dileep.ecommerce.repository.ProductRepository;
import com.dileep.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // ADD TO CART
    // =========================

    @Transactional
    public CartResponse addToCart(
            Long userId,
            AddToCartRequest request) {

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

        if (request.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return convertToResponse(cart);
    }

    // =========================
    // GET CART
    // =========================

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        ));

        return convertToResponse(cart);
    }

    // =========================
    // UPDATE CART ITEM
    // =========================

    @Transactional
    public CartResponse updateCartItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        ));

        CartItem cartItem =
                cartItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found"
                                ));

        // Security check
        if (!cartItem.getCart()
                .getId()
                .equals(cart.getId())) {

            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }

        if (request.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        Product product = cartItem.getProduct();

        if (request.getQuantity() >
                product.getStockQuantity()) {

            throw new RuntimeException(
                    "Insufficient stock"
            );
        }

        cartItem.setQuantity(
                request.getQuantity()
        );

        cartItemRepository.save(cartItem);

        return convertToResponse(cart);
    }

    // =========================
    // REMOVE CART ITEM
    // =========================

    @Transactional
    public CartResponse removeCartItem(
            Long userId,
            Long itemId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        ));

        CartItem cartItem =
                cartItemRepository.findById(itemId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found"
                                ));

        // Security check
        if (!cartItem.getCart()
                .getId()
                .equals(cart.getId())) {

            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }

        // Delete ONLY the selected cart item
        cartItemRepository.deleteById(itemId);

        // Execute DELETE immediately
        cartItemRepository.flush();

        // Return the cart using fresh database data
        return convertToResponse(cart);
    }

    // =========================
    // CONVERT CART TO RESPONSE
    // =========================

    private CartResponse convertToResponse(
            Cart cart) {

        // Always fetch the latest cart items
        // directly from the database.
        List<CartItem> cartItems =
                cartItemRepository.findAllByCartId(
                        cart.getId()
                );

        List<CartItemResponse> items =
                cartItems.stream()
                        .map(this::convertItemToResponse)
                        .toList();

        // Calculate total from the fresh database list
        BigDecimal totalPrice =
                items.stream()
                        .map(CartItemResponse::getSubtotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                items,
                totalPrice
        );
    }

    // =========================
    // CONVERT CART ITEM
    // =========================

    private CartItemResponse convertItemToResponse(
            CartItem cartItem) {

        Product product = cartItem.getProduct();

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                cartItem.getQuantity(),
                cartItem.getSubtotal()
        );
    }
}