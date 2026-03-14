package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.DashboardResponse;
import com.example.CampusConnectMP.controller.dto.ProductResponse;
import com.example.CampusConnectMP.controller.dto.UserResponse;
import com.example.CampusConnectMP.service.ChatService;
import com.example.CampusConnectMP.service.ProductService;
import com.example.CampusConnectMP.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;
    private final UserService userService;
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getMyDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        if (userEmail == null || userEmail.isBlank()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                userEmail = userDetails.getUsername();
            }
        }

        Long userId = userService.getUserIdByEmail(userEmail);
        UserResponse userInfo = userService.getUserInfoByEmail(userEmail);

        if (userInfo.getName() == null || userInfo.getName().isBlank()) {
            // fallback to email username portion when name missing
            userInfo.setName(userEmail != null ? userEmail.split("@")[0] : "");
        }
        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            userInfo.setEmail(userEmail != null ? userEmail : "");
        }
        List<ProductResponse> myListings = productService.getProductsBySeller(userId);
        List<ProductResponse> myPurchases = productService.getProductsByBuyer(userId);

        List<Long> contactedProductIds = chatService.getContactedProductIds(userId);
        List<ProductResponse> contactedProducts = contactedProductIds.stream()
                .map(productService::getProductById)
                .collect(Collectors.toList());

        long totalProductsListed = productService.getTotalProductsBySeller(userId);
        long totalChats = chatService.getTotalConversations(userId);

        DashboardResponse response = DashboardResponse.builder()
                .userId(userInfo.getId())
                .userName((userInfo.getName() != null && !userInfo.getName().isBlank()) ? userInfo.getName() : (userEmail != null ? userEmail.split("@")[0] : "User"))
                .userEmail((userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) ? userInfo.getEmail() : userEmail)
                .myListings(myListings)
                .myPurchases(myPurchases)
                .contactedProducts(contactedProducts)
                .totalProductsListed(totalProductsListed)
                .totalChats(totalChats)
                .build();

        return ResponseEntity.ok(response);
    }
}