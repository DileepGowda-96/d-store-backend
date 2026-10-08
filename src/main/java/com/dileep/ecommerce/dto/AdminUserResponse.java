package com.dileep.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
}