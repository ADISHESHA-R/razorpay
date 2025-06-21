package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.CartItem;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    private static final String CART_SESSION_KEY = "cart";

    public void addProductToCart(Long productId, int quantity, HttpSession session) {
        List<CartItem> cart = getCart(session);
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return;
                }
            }
            cart.add(new CartItem(product, quantity));
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void removeProductFromCart(Long productId, HttpSession session) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.setAttribute(CART_SESSION_KEY, new ArrayList<CartItem>());
    }

    public void updateProductQuantity(Long productId, int quantity, HttpSession session) {
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }
}
