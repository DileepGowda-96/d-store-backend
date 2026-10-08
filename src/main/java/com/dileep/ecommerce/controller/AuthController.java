package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.LoginRequest;
import com.dileep.ecommerce.dto.LoginResponse;
import com.dileep.ecommerce.dto.RegisterRequest;
import com.dileep.ecommerce.dto.UserResponse;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        User user = authService.getUserByEmail(request.getEmail());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                userResponse
        );

        return ResponseEntity.ok(response);
    }
}