package com.example.CampusConnectMP.service;

import com.example.CampusConnectMP.exception.PaymentProcessingException;
import com.example.CampusConnectMP.exception.ResourceNotFoundException;
import com.example.CampusConnectMP.model.Order;
import com.example.CampusConnectMP.model.OrderStatus;
import com.example.CampusConnectMP.model.Product;
import com.example.CampusConnectMP.model.User;
import com.example.CampusConnectMP.repository.OrderRepository;
import com.example.CampusConnectMP.repository.ProductRepository;
import com.example.CampusConnectMP.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Value("${payment.currency:INR}")
    private String currency;

    @Transactional
    public Map<String, Object> createOrder(Long productId, String buyerEmail) {
        return startFakeOrder(productId, buyerEmail);
    }

    @Transactional
    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        throw new PaymentProcessingException("Razorpay payment verification is disabled. Use fake payment endpoints.");
    }

    @Transactional
    public Map<String, Object> handleWebhook(String payload, String signatureHeader) {
        throw new PaymentProcessingException("Webhook handling is disabled in mock payment mode.");
    }

    public List<Map<String, Object>> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByBuyerId(user.getId()).stream()
                .map(order -> {
                    Product product = productRepository.findById(order.getProductId()).orElse(null);
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", order.getId());
                    orderMap.put("productTitle", product != null ? product.getTitle() : "Unknown");
                    orderMap.put("amount", order.getAmount());
                    orderMap.put("status", order.getStatus().toString());
                    orderMap.put("paymentDate", order.getPaymentDate());
                    orderMap.put("createdAt", order.getCreatedAt());
                    orderMap.put("razorpayOrderId", order.getRazorpayOrderId());
                    orderMap.put("razorpayPaymentId", order.getRazorpayPaymentId());
                    orderMap.put("buyerId", order.getBuyerId());
                    orderMap.put("sellerId", order.getSellerId());
                    return orderMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> startFakeOrder(Long productId, String buyerEmail) {
        if (productId == null || productId <= 0) {
            throw new PaymentProcessingException("Invalid productId");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.isSold()) {
            throw new PaymentProcessingException("Product already sold");
        }

        User buyer;
        if (buyerEmail == null || buyerEmail.isBlank()) {
            buyer = findOrCreateGuest();
        } else {
            buyer = userRepository.findByEmail(buyerEmail).orElseGet(this::findOrCreateGuest);
        }

        if (buyer.getId().equals(product.getSeller().getId())) {
            throw new PaymentProcessingException("Seller cannot buy their own product");
        }

        long amountInPaise = Math.round(product.getPrice() * 100);

        Order order = Order.builder()
                .productId(productId)
                .buyerId(buyer.getId())
                .sellerId(product.getSeller().getId())
                .amount(product.getPrice())
                .status(OrderStatus.PENDING)
                .razorpayOrderId("fake_" + UUID.randomUUID())
                .build();

        orderRepository.save(order);

        return Map.of(
                "orderId", order.getId(),
                "amount", amountInPaise,
                "currency", currency,
                "status", "PENDING",
                "message", "Order created. Call /api/orders/{orderId}/pay to complete payment"
        );
    }

    @Transactional
    public Map<String, Object> confirmFakePayment(Long orderId, String buyerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User requester;
        if (buyerEmail == null || buyerEmail.isBlank()) {
            requester = findOrCreateGuest();
        } else {
            requester = userRepository.findByEmail(buyerEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));
        }

        if (!order.getBuyerId().equals(requester.getId())) {
            throw new PaymentProcessingException("Unauthorized fake payment confirmation");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentProcessingException("Order cannot be confirmed in status " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentDate(LocalDateTime.now());
        orderRepository.save(order);

        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setSold(true);
        product.setBuyer(userRepository.findById(order.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found")));
        productRepository.save(product);

        return Map.of(
                "orderId", order.getId(),
                "status", "PAID",
                "message", "Payment successful ✅"
        );
    }

    @Transactional
    public Map<String, Object> fakeBuyNow(Long productId, String buyerEmail) {
        Map<String, Object> startResult = startFakeOrder(productId, buyerEmail);
        Long orderId = ((Number) startResult.get("orderId")).longValue();
        Map<String, Object> confirmResult = confirmFakePayment(orderId, buyerEmail);

        return Map.of(
                "start", startResult,
                "confirm", confirmResult,
                "status", "PAID",
                "message", "Fake payment flow completed"
        );
    }

    private User findOrCreateGuest() {
        String guestEmail = "guest@demo.com";
        return userRepository.findByEmail(guestEmail)
                .orElseGet(() -> {
                    User guest = User.builder()
                            .email(guestEmail)
                            .name("Guest User")
                            .password("")
                            .blocked(false)
                            .build();
                    return userRepository.save(guest);
                });
    }
}
