package com.dileep.ecommerce.controller;

import com.dileep.ecommerce.dto.AdminUserResponse;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {

        List<AdminUserResponse> users = userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return ResponseEntity.ok(
                convertToResponse(user)
        );
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (!role.equals("CUSTOMER") &&
                !role.equals("ADMIN")) {

            throw new RuntimeException(
                    "Role must be CUSTOMER or ADMIN"
            );
        }

        // Prevent removing the last ADMIN
        if (user.getRole().equals("ADMIN") &&
                role.equals("CUSTOMER") &&
                userRepository.countByRole("ADMIN") <= 1) {

            throw new RuntimeException(
                    "Cannot remove the last ADMIN user"
            );
        }

        user.setRole(role);

        return ResponseEntity.ok(
                convertToResponse(
                        userRepository.save(user)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // Prevent deleting the last ADMIN
        if (user.getRole().equals("ADMIN") &&
                userRepository.countByRole("ADMIN") <= 1) {

            throw new RuntimeException(
                    "Cannot delete the last ADMIN user"
            );
        }

        userRepository.delete(user);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }

    private AdminUserResponse convertToResponse(User user) {

        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}