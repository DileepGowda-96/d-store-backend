package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
}