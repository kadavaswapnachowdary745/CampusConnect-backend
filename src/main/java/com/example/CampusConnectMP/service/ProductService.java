package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.ProductRequest;
import com.example.CampusConnectMP.controller.dto.ProductResponse;
import com.example.CampusConnectMP.model.Category;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileHandlingService fileHandlingService;

    public ProductResponse addProduct(ProductRequest request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        String imagePath = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imagePath = fileHandlingService.saveFile(request.getImage());
        }

        Product product = Product.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imagePath(imagePath)
                .seller(seller)
                .sold(false)
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByBuyer(Long buyerId) {
        return productRepository.findByBuyerId(buyerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getTotalProductsBySeller(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }

    public long getProductsSold() {
        return productRepository.countBySoldTrue();
    }

    public long getActiveListings() {
        return productRepository.countBySoldFalse();
    }

    public Map<String, Long> getProductsPerCategory() {
        return productRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().name(), Collectors.counting()));
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public List<ProductResponse> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteProduct(Long id, String requesterEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only owner can delete
        if (!product.getSeller().getId().equals(requester.getId())) {
            throw new RuntimeException("Not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    public ProductResponse buyProduct(Long productId, String buyerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Seller cannot buy their own product");
        }

        if (product.isSold()) {
            throw new RuntimeException("Product already sold");
        }

        product.setSold(true);
        product.setBuyer(buyer);
        productRepository.save(product);

        return mapToResponse(product);
    }

    public ProductResponse updateProduct(Long productId, ProductRequest request, String requesterEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only owner can update
        if (!product.getSeller().getId().equals(requester.getId())) {
            throw new RuntimeException("Not authorized to update this product");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imagePath = fileHandlingService.saveFile(request.getImage());
            product.setImagePath(imagePath);
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imagePath(product.getImagePath())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getName())
                .buyerId(product.getBuyer() != null ? product.getBuyer().getId() : null)
                .createdAt(product.getCreatedAt())
                .sold(product.isSold())
                .build();
    }
}
