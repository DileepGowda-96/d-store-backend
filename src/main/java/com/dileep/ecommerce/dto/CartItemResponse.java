package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal subtotal;
}