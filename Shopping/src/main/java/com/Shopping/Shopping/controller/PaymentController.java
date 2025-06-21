package com.Shopping.Shopping.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.Shopping.Shopping.model.Orders;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.OrdersRepository;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public PaymentController(OrdersRepository ordersRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository) {
        this.ordersRepository = ordersRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // 🟢 Buy Now - not used in cart flow
    @PostMapping("/buy-now/{productId}")
    public String buyNow(@PathVariable Long productId,
                         @RequestParam(defaultValue = "1") int quantity,
                         Principal principal,
                         Model model) throws RazorpayException {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int amountInPaise = (int) (product.getPrice() * quantity * 100);

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        model.addAttribute("key", razorpayKey);
        model.addAttribute("amount", amountInPaise / 100);
        model.addAttribute("orderId", order.get("id"));
        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);

        return "razorpay_checkout";
    }

    // 🟢 AJAX: Create Razorpay Order from Total Amount
    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
        int amount = (int) data.get("amount");

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject options = new JSONObject();
        options.put("amount", amount);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        return ResponseEntity.ok(response);
    }

    // 🟢 Handle Payment Success
    @PostMapping("/payment-success")
    @ResponseBody
    public ResponseEntity<?> handlePayment(@RequestBody Map<String, String> data, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Orders order = new Orders();
        order.setRazorpayPaymentId(data.get("razorpay_payment_id"));
        order.setRazorpayOrderId(data.get("razorpay_order_id"));
        order.setRazorpaySignature(data.get("razorpay_signature"));
        order.setAmount(500); // optional: make dynamic if needed
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);
        order.setEmail(user.getUsername());

        ordersRepository.save(order);
        return ResponseEntity.ok().build();
    }

    // 🟢 Payment Success Page
    @GetMapping("/payment-success-page")
    public String paymentSuccessPage() {
        return "payment_success";
    }
}
