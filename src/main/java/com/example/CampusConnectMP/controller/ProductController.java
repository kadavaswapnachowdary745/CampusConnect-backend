package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.ProductRequest;
import com.example.CampusConnectMP.controller.dto.ProductResponse;
import com.example.CampusConnectMP.model.Category;
import com.example.CampusConnectMP.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @ModelAttribute ProductRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(productService.addProduct(request, userEmail));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Category category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(productService.updateProduct(id, request, userEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        productService.deleteProduct(id, userEmail);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<ProductResponse> buyProduct(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(productService.buyProduct(id, userEmail));
    }
}
