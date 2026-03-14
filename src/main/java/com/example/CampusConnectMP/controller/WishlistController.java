package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.WishlistResponse;
import com.example.CampusConnectMP.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable Long productId, Authentication authentication) {
        String userEmail = authentication.getName();
        wishlistService.addToWishlist(productId, userEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getUserWishlist(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(wishlistService.getUserWishlist(userEmail));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId, Authentication authentication) {
        String userEmail = authentication.getName();
        wishlistService.removeFromWishlist(productId, userEmail);
        return ResponseEntity.ok().build();
    }
}
