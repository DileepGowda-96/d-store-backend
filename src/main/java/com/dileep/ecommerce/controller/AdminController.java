package com.dileep.ecommerce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard(Authentication authentication) {

        return "Welcome Admin! Logged in as "
                + authentication.getName();
    }
}