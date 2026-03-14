package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.ProductResponse;
import com.example.CampusConnectMP.controller.dto.WishlistResponse;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.model.Wishlist;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import com.example.CampusConnectMP.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public void addToWishlist(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new RuntimeException("Product already in wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);

        String notification = String.format("Your product '%s' was added to wishlist by %s", product.getTitle(), user.getName());
        notificationService.notifyUser(product.getSeller().getId(), notification);
    }

    public List<WishlistResponse> getUserWishlist(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void removeFromWishlist(Long productId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        wishlistRepository.delete(wishlist);
    }

    private WishlistResponse mapToResponse(Wishlist wishlist) {
        Product p = wishlist.getProduct();
        ProductResponse productResponse = ProductResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .category(p.getCategory())
                .imagePath(p.getImagePath())
                .sellerId(p.getSeller().getId())
                .sellerName(p.getSeller().getName())
                .build();

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .product(productResponse)
                .addedAt(wishlist.getCreatedAt())
                .build();
    }
}
