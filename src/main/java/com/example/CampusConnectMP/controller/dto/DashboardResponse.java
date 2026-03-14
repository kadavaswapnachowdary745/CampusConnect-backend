package com.example.CampusConnectMP.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private List<ProductResponse> myListings;
    private List<ProductResponse> myPurchases;
    private List<ProductResponse> contactedProducts;
    private long totalProductsListed;
    private long totalChats;
}
