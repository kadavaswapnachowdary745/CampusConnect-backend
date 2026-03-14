package com.example.CampusConnectMP.repository;

import com.example.CampusConnectMP.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
}
