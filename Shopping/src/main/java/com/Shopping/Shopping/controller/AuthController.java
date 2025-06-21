package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Shopping.Shopping.service.ProductService;

import java.io.IOException;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SellerRepository sellerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductService productService;

    // User Signup Form
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // User Signup Submit
    @PostMapping("/signup")
    public String signupSubmit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "alternateNumber", required = false) String alternateNumber,
            @RequestParam("address") String address,
            @RequestParam("photo") MultipartFile photoFile,
            Model model) {

        if (userRepo.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("user", new User());
            return "signup";
        }

        if (!isValidPassword(password)) {
            model.addAttribute("error", "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, digit, and special character.");
            model.addAttribute("user", new User());
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setAlternateNumber(alternateNumber);
        user.setAddress(address);

        try {
            user.setPhoto(photoFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        userRepo.save(user);
        return "redirect:/login";
    }

    // Seller Signup Form
    @GetMapping("/seller-signup")
    public String sellerSignupForm(Model model) {
        model.addAttribute("seller", new Seller());
        return "seller-signup";
    }

    // Seller Signup Submit
    @PostMapping("/seller-signup")
    public String sellerSignupSubmit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("whatsappNumber") String whatsappNumber,
            @RequestParam("businessEmail") String businessEmail,
            @RequestParam("gstNumber") String gstNumber,
            @RequestParam("photo") MultipartFile photoFile,
            Model model) {

        if (sellerRepo.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("seller", new Seller());
            return "seller-signup";
        }

        Seller seller = new Seller();
        seller.setUsername(username);
        seller.setPassword(passwordEncoder.encode(password));
        seller.setEmail(email);
        seller.setWhatsappNumber(whatsappNumber);
        seller.setBusinessEmail(businessEmail);
        seller.setGstNumber(gstNumber);

        try {
            seller.setPhoto(photoFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sellerRepo.save(seller);
        return "redirect:/seller-login";
    }

    // User Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Seller Login Page
    @GetMapping("/seller-login")
    public String sellerLoginPage() {
        return "seller-login";
    }

    // Profile for Users
    @GetMapping("/profile")
    public String profileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("userBase64", user.getPhotoBase64());
        return "profile";
    }

    // Profile for Sellers
    @GetMapping("/seller-profile")
    public String sellerProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        model.addAttribute("seller", seller);
        model.addAttribute("sellerBase64", seller.getPhotoBase64());
        return "seller-profile";
    }

    // Profile Update for Users
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("alternateNumber") String alternateNumber,
                                @RequestParam("address") String address,
                                @RequestParam("photo") MultipartFile photo) {
        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAlternateNumber(alternateNumber);
        user.setAddress(address);
        try {
            if (!photo.isEmpty()) {
                user.setPhoto(photo.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        userRepo.save(user);
        return "redirect:/profile";
    }

    // Profile Update for Sellers
    @PostMapping("/seller-profile")
    public String updateSellerProfile(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam("whatsappNumber") String whatsappNumber,
                                      @RequestParam("businessEmail") String businessEmail,
                                      @RequestParam("gstNumber") String gstNumber,
                                      @RequestParam("photo") MultipartFile photo) {
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        seller.setWhatsappNumber(whatsappNumber);
        seller.setBusinessEmail(businessEmail);
        seller.setGstNumber(gstNumber);
        try {
            if (!photo.isEmpty()) {
                seller.setPhoto(photo.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sellerRepo.save(seller);
        return "redirect:/seller-profile";
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Add user and product list to the model
        model.addAttribute("user", user);
        model.addAttribute("products", productService.getAllProducts());

        return "home";
    }

    // Home for Sellers
    @GetMapping("/seller-home")
    public String sellerHome(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/seller-login";
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        model.addAttribute("seller", seller);
        return "seller-home";
    }

    // Password validation logic
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()_+=<>?].*");
    }
}
