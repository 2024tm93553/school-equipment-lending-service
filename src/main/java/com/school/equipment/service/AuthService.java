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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return new UserResponse(
            savedUser.getUserId(),
            savedUser.getUsername(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }

    public LoginResponse login(LoginRequest request) throws InvalidCredentialsException {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
            user.getUsername(),
            user.getRole().name(),
            user.getUserId()
        );

        return new LoginResponse(
            token,
            user.getRole(),
            user.getFullName(),
            user.getUserId()
        );
    }
}
