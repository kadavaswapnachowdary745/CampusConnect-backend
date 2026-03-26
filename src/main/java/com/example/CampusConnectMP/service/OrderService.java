package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.controller.dto.OrderResponse;
import com.example.CampusConnectMP.exception.ResourceNotFoundException;
import com.example.CampusConnectMP.model.Order;
import com.example.CampusConnectMP.model.OrderStatus;
import com.example.CampusConnectMP.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) {
            return OrderResponse.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .paymentDate(order.getPaymentDate())
                    .message("Order already paid")
                    .build();
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentDate(LocalDateTime.now());
        orderRepository.save(order);

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .paymentDate(order.getPaymentDate())
                .message("Payment successful")
                .build();
    }
}
