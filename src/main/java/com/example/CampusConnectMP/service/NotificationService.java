package com.example.CampusConnectMP.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, List<String>> notificationMap = new ConcurrentHashMap<>();

    public void notifyUser(Long userId, String message) {
        notificationMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", message);
    }

    public List<String> getNotifications(Long userId) {
        return new ArrayList<>(notificationMap.getOrDefault(userId, new ArrayList<>()));
    }

    public void clearNotifications(Long userId) {
        notificationMap.remove(userId);
    }
}
