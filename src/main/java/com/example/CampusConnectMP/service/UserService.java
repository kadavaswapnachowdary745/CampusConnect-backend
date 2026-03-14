package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.UserResponse;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBlocked(true);
        userRepository.save(user);
    }
    
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public Map<String, Long> getUsersPerMonth() {
        return userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().getYear() + "-" + String.format("%02d", u.getCreatedAt().getMonthValue()),
                        Collectors.counting()
                ));
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    public UserResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .blocked(user.isBlocked())
                .build();
    }
}
