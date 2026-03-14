package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.model.Order;
import com.example.CampusConnectMP.model.PaymentStatus;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.OrderRepository;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    public Map<String, Object> createOrder(Long productId, String buyerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.isSold()) {
            throw new RuntimeException("Product already sold");
        }

        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new RuntimeException("Seller cannot buy their own product");
        }

        long amountInPaise = Math.round(product.getPrice() * 100);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "order_rcptid_" + productId);
        orderRequest.put("payment_capture", 1);

        com.razorpay.Order razorpayOrder;
        try {
            razorpayOrder = razorpayClient.orders.create(orderRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }

        Order order = Order.builder()
                .productId(productId)
                .buyerId(buyer.getId())
                .sellerId(product.getSeller().getId())
                .amount(product.getPrice())
                .paymentStatus(PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrder.get("id"))
                .build();

        orderRepository.save(order);

        return Map.of(
                "orderId", razorpayOrder.get("id"),
                "amount", amountInPaise,
                "currency", currency,
                "keyId", razorpayKeyId
        );
    }

    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", razorpayOrderId);
        options.put("razorpay_payment_id", razorpayPaymentId);
        options.put("razorpay_signature", razorpaySignature);

        try {
            Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (Exception e) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Invalid payment signature");
        }

        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setRazorpayPaymentId(razorpayPaymentId);
        orderRepository.save(order);

        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setSold(true);
        product.setBuyer(userRepository.findById(order.getBuyerId()).orElse(null));
        productRepository.save(product);
    }

    public List<Map<String, Object>> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByBuyerId(user.getId()).stream()
                .map(order -> {
                    Product product = productRepository.findById(order.getProductId()).orElse(null);
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", order.getId());
                    orderMap.put("productTitle", product != null ? product.getTitle() : "Unknown");
                    orderMap.put("amount", order.getAmount());
                    orderMap.put("paymentStatus", order.getPaymentStatus().toString());
                    orderMap.put("createdAt", order.getCreatedAt());
                    orderMap.put("razorpayOrderId", order.getRazorpayOrderId());
                    orderMap.put("razorpayPaymentId", order.getRazorpayPaymentId());
                    return orderMap;
                })
                .collect(Collectors.toList());
    }
}