package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
}