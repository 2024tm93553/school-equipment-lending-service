package com.school.equipment.controller;

import com.school.equipment.dto.user.RegisterRequest;
import com.school.equipment.dto.user.UserResponse;
import com.school.equipment.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
