package com.example.CampusConnectMP.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WishlistResponse {
    private Long id;
    private ProductResponse product;
    private LocalDateTime addedAt;
}
