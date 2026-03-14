package com.example.CampusConnectMP.controller.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long receiverId;
    private Long productId; // Optional, to link chat to a specific product
    private String content;
}
