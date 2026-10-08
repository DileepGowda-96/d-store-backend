package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class WishlistProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
}