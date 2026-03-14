package com.example.CampusConnectMP.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private Long totalUsers;
    private Long totalProducts;
    private Long productsSold;
    private Long activeListings;
    private List<ReportDto> reports;
    private Map<String, Long> usersPerMonth;
    private Map<String, Long> productsPerCategory;
}
