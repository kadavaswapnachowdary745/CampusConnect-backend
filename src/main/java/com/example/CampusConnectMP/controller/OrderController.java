package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.OrderResponse;
import com.example.CampusConnectMP.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable("id") Long orderId) {
        OrderResponse response = orderService.payOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
