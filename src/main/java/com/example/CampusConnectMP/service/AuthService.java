package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.AuthResponse;
import com.example.CampusConnectMP.controller.dto.LoginRequest;
import com.example.CampusConnectMP.controller.dto.RefreshTokenRequest;
import com.example.CampusConnectMP.controller.dto.RegisterRequest;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.UserRepository;
import com.example.CampusConnectMP.security.CustomUserDetails;
import com.example.CampusConnectMP.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${auth.allowed-domains:college.edu}")
    private String allowedDomains; // comma-separated 

    private List<String> getAllowedDomainList() {
        return Arrays.stream(allowedDomains.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private boolean isCollegeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        List<String> allowed = getAllowedDomainList();
        if (allowed.isEmpty() || allowed.contains("*") || allowed.contains("any")) {
            return true;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return allowed.stream().anyMatch(domain::endsWith);
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (!isCollegeEmail(request.getEmail())) {
            throw new IllegalArgumentException("Registration is allowed only with college email addresses");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .blocked(false)
                .build();

        userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isBlocked()) {
            throw new RuntimeException("User account is blocked.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token user"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token expired or invalid");
        }

        String newToken = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
