package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.ChatMessageRequest;
import com.example.CampusConnectMP.controller.dto.ChatMessageResponse;
import com.example.CampusConnectMP.controller.dto.UnreadCountResponse;
import com.example.CampusConnectMP.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import com.example.CampusConnectMP.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessageWs(@Payload ChatMessageRequest chatMessageRequest, Authentication authentication) {
        String senderEmail = authentication.getName();
        ChatMessageResponse response = chatService.saveMessage(chatMessageRequest, senderEmail);

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                response.getReceiverId().toString(), "/queue/messages", response
        );

        // Also send back to sender for confirmation
        messagingTemplate.convertAndSendToUser(
                response.getSenderId().toString(), "/queue/messages", response
        );

        // Send updated unread counts to the receiver
        List<UnreadCountResponse> unreadCounts = chatService.getUnreadCountsForReceiver(response.getReceiverId(), null)
                .stream()
                .map(c -> new UnreadCountResponse(c.getSenderId(), c.getProductId(), c.getUnreadCount()))
                .toList();
        messagingTemplate.convertAndSendToUser(response.getReceiverId().toString(), "/queue/unread-counts", unreadCounts);
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest chatMessageRequest, Authentication authentication) {
        String senderEmail = authentication.getName();
        ChatMessageResponse response = chatService.saveMessage(chatMessageRequest, senderEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversationHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) Long productId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(chatService.getConversation(userId, userEmail, productId));
    }

    @PutMapping("/mark-read/{userId}")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long userId,
            @RequestParam(required = false) Long productId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        chatService.markMessagesAsRead(userId, userEmail, productId);

        // Notify the current user about updated unread counts
        List<UnreadCountResponse> unreadCounts = chatService.getUnreadCounts(userEmail, productId)
                .stream()
                .map(c -> new UnreadCountResponse(c.getSenderId(), c.getProductId(), c.getUnreadCount()))
                .toList();
        messagingTemplate.convertAndSendToUser(
                userRepository.findByEmail(userEmail).orElseThrow().getId().toString(), "/queue/unread-counts", unreadCounts
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-counts")
    public ResponseEntity<List<UnreadCountResponse>> getUnreadCounts(
            @RequestParam(required = false) Long productId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<UnreadCountResponse> unreadCounts = chatService.getUnreadCounts(userEmail, productId)
                .stream()
                .map(c -> new UnreadCountResponse(c.getSenderId(), c.getProductId(), c.getUnreadCount()))
                .toList();
        return ResponseEntity.ok(unreadCounts);
    }
}
