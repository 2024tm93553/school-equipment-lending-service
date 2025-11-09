package com.school.equipment.service;

import com.school.equipment.dto.user.LoginRequest;
import com.school.equipment.dto.user.LoginResponse;
import com.school.equipment.dto.user.RegisterRequest;
import com.school.equipment.dto.user.UserResponse;
import com.school.equipment.entity.User;
import com.school.equipment.exception.InvalidCredentialsException;
import com.school.equipment.exception.UserAlreadyExistsException;
import com.school.equipment.repository.UserRepository;
import com.school.equipment.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse register(RegisterRequest request) throws UserAlreadyExistsException {
        log.info("Registration attempt for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully - username: {}, role: {}, userId: {}",
                savedUser.getUsername(), savedUser.getRole(), savedUser.getUserId());

        return new UserResponse(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }

    public LoginResponse login(LoginRequest request) throws InvalidCredentialsException {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.warn("Login failed - user not found: {}", request.getUsername());
                return new InvalidCredentialsException("Invalid username or password");
            });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed - invalid password for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getRole().name(),
            user.getUserId()
        );

        log.info("Login successful - username: {}, role: {}, userId: {}",
                user.getUsername(), user.getRole(), user.getUserId());

        return new LoginResponse(
            token,
            user.getRole(),
            user.getFullName(),
            user.getUserId()
        );
    }
}
