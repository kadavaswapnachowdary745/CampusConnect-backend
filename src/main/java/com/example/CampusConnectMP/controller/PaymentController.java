package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Long productId = request.get("productId");
        Map<String, Object> orderData = paymentService.createOrder(productId, userEmail);
        return ResponseEntity.ok(orderData);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String razorpayOrderId = request.get("razorpayOrderId");
        String razorpayPaymentId = request.get("razorpayPaymentId");
        String razorpaySignature = request.get("razorpaySignature");
        paymentService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        return ResponseEntity.ok("Payment verified successfully");
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Map<String, Object>> orders = paymentService.getUserOrders(userEmail);
        return ResponseEntity.ok(orders);
    }
}