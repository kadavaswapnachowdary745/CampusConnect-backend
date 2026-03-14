package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.ChatMessageRequest;
import com.example.CampusConnectMP.controller.dto.ChatMessageResponse;
import com.example.CampusConnectMP.model.ChatMessage;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.ChatMessageRepository;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ChatMessageResponse saveMessage(ChatMessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElse(null);
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .product(product)
                .content(request.getContent())
                .isRead(false)
                .build();

        chatMessageRepository.save(message);

        String notification = String.format("New message from %s: %s", sender.getName(), request.getContent());
        notificationService.notifyUser(receiver.getId(), notification);

        return mapToResponse(message);
    }

    public List<ChatMessageResponse> getConversation(Long user2Id, String user1Email, Long productId) {
        User user1 = userRepository.findByEmail(user1Email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (productId == null) {
            return chatMessageRepository.findConversation(user1.getId(), user2Id)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return chatMessageRepository.findConversationAboutProduct(user1.getId(), user2Id, productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public int markMessagesAsRead(Long otherUserId, String currentUserEmail, Long productId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatMessageRepository.markMessagesAsRead(currentUser.getId(), otherUserId, productId);
    }

    public List<UnreadCount> getUnreadCounts(String currentUserEmail, Long productId) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return getUnreadCountsForReceiver(currentUser.getId(), productId);
    }

    public List<UnreadCount> getUnreadCountsForReceiver(Long receiverId, Long productId) {
        return chatMessageRepository.countUnreadBySenderAndProduct(receiverId)
                .stream()
                .map(row -> {
                    Long senderId = ((Number) row[0]).longValue();
                    Long foundProductId = row[1] != null ? ((Number) row[1]).longValue() : null;
                    Long unreadCount = ((Number) row[2]).longValue();
                    return new UnreadCount(senderId, foundProductId, unreadCount);
                })
                .filter(uc -> productId == null || (uc.getProductId() != null ? uc.getProductId().equals(productId) : productId == null))
                .collect(Collectors.toList());
    }

    public long getTotalConversations(Long userId) {
        return chatMessageRepository.findDistinctChatParticipantIds(userId).size();
    }

    public List<Long> getContactedProductIds(Long userId) {
        return chatMessageRepository.findContactedProductIds(userId);
    }

    public static class UnreadCount {
        private final Long senderId;
        private final Long productId;
        private final Long unreadCount;

        public UnreadCount(Long senderId, Long productId, Long unreadCount) {
            this.senderId = senderId;
            this.productId = productId;
            this.unreadCount = unreadCount;
        }

        public Long getSenderId() {
            return senderId;
        }

        public Long getProductId() {
            return productId;
        }

        public Long getUnreadCount() {
            return unreadCount;
        }
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getName())
                .productId(message.getProduct() != null ? message.getProduct().getId() : null)
                .productTitle(message.getProduct() != null ? message.getProduct().getTitle() : null)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
