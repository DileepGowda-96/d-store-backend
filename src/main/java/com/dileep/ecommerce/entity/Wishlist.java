package com.dileep.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(
            mappedBy = "wishlist",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<WishlistProduct> wishlistProducts = new ArrayList<>();

    public List<Product> getProducts() {
        return wishlistProducts.stream()
                .map(WishlistProduct::getProduct)
                .toList();
    }

    public void addProduct(Product product) {
        boolean exists = wishlistProducts.stream()
                .anyMatch(item -> item.getProduct().getId()
                        .equals(product.getId()));

        if (exists) {
            throw new RuntimeException(
                    "Product already exists in wishlist"
            );
        }

        WishlistProduct wishlistProduct = new WishlistProduct();
        wishlistProduct.setWishlist(this);
        wishlistProduct.setProduct(product);

        wishlistProducts.add(wishlistProduct);
    }

    public void removeProduct(Product product) {
        wishlistProducts.removeIf(item ->
                item.getProduct().getId().equals(product.getId())
        );
    }
}