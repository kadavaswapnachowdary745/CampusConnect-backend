package com.example.CampusConnectMP.controller.dto;

import com.example.CampusConnectMP.model.Category;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Category category;
    private String imagePath;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private LocalDateTime createdAt;
    private boolean sold;
}
