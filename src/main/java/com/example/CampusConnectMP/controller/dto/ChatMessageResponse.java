package com.example.CampusConnectMP.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Long productId;
    private String productTitle;
    private String content;
    private LocalDateTime timestamp;
}
