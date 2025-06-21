package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.CartItem;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private ProductService productService;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");
        if (cartItems == null) cartItems = new ArrayList<>();

        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("key", "rzp_test_E1v7jL3XBuxjgO"); // ✅ Inject Razorpay key here

        return "checkout";
    }


    // ✅ Updated to accept custom quantity
    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(name = "quantity", required = false, defaultValue = "1") int quantity,
                            HttpSession session) {
        Product product = productService.getProductById(id);
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(id)) {
                item.setQuantity(item.getQuantity() + quantity);  // Add the passed quantity
                found = true;
                break;
            }
        }

        if (!found) {
            cart.add(new CartItem(product, quantity));
        }

        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> item.getProduct().getId().equals(id));
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam int quantity,
                                 HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(id)) {
                    item.setQuantity(quantity);
                    break;
                }
            }
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

}
