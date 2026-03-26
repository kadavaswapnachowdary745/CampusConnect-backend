package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.service.PaymentService;
import com.example.CampusConnectMP.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class LegacyPaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestParam("productId") Long productId,
                                                           @RequestParam(value = "buyerEmail", required = false) String buyerEmail) {
        Map<String, Object> result = paymentService.createOrder(productId, buyerEmail);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmPayment(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.payOrder(orderId));
    }
}
