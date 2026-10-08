package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishlistResponse {

    private Long id;
    private Long userId;
    private List<WishlistProductResponse> products;
}