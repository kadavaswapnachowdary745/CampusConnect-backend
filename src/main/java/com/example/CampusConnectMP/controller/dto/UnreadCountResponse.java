package com.example.CampusConnectMP.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnreadCountResponse {
    private Long senderId;
    private Long productId;
    private Long unreadCount;
}
