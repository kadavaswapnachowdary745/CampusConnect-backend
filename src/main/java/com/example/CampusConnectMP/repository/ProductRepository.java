package com.example.CampusConnectMP.repository;

import com.example.CampusConnectMP.model.Category;
import com.example.CampusConnectMP.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByTitleContainingIgnoreCase(String keyword);
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByBuyerId(Long buyerId);

    long countBySoldTrue();
    long countBySoldFalse();
    long countBySellerId(Long sellerId);
}
