package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.LoginRequest;
import com.dileep.ecommerce.dto.RegisterRequest;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.UserRepository;
import com.dileep.ecommerce.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered"
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role("CUSTOMER")
                .build();

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }
}