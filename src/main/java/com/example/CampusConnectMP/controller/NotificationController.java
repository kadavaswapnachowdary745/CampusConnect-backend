package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.service.NotificationService;
import com.example.CampusConnectMP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<String>> getNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        Long userId = userService.getUserIdByEmail(userEmail);

        List<String> notifications = notificationService.getNotifications(userId);
        notificationService.clearNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}
