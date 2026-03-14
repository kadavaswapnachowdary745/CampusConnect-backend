package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.AnalyticsResponse;
import com.example.CampusConnectMP.controller.dto.ProductResponse;
import com.example.CampusConnectMP.controller.dto.ReportDto;
import com.example.CampusConnectMP.controller.dto.UserResponse;
import com.example.CampusConnectMP.service.ProductService;
import com.example.CampusConnectMP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        long totalUsers = userService.getTotalUsers();
        long totalProducts = productService.getTotalProducts();
        long productsSold = productService.getProductsSold();
        long activeListings = productService.getActiveListings();

        Map<String, Long> usersPerMonth = userService.getUsersPerMonth();
        Map<String, Long> productsPerCategory = productService.getProductsPerCategory();

        List<ReportDto> reports = List.of(
                ReportDto.builder()
                        .title("Top category")
                        .value(productsPerCategory.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("N/A"))
                        .description("Category with the most active listings")
                        .build(),
                ReportDto.builder()
                        .title("Products not sold yet")
                        .value(String.valueOf(activeListings))
                        .description("Current active listings (not sold)")
                        .build(),
                ReportDto.builder()
                        .title("Products sold")
                        .value(String.valueOf(productsSold))
                        .description("Number of products marked as sold")
                        .build()
        );

        AnalyticsResponse analytics = AnalyticsResponse.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .productsSold(productsSold)
                .activeListings(activeListings)
                .usersPerMonth(usersPerMonth)
                .productsPerCategory(productsPerCategory)
                .reports(reports)
                .build();

        return ResponseEntity.ok(analytics);
    }
    
    // Deleting a product as admin relies on the regular ProductController's DELETE method
    // Since we check if requester is ADMIN in the ProductService.deleteProduct method
}
