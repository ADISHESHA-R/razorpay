package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/seller-dashboard")
    public String sellerDashboard() {
        return "seller-dashboard";
    }

    @PostMapping("/upload-product")
    public String uploadProduct(@RequestParam String productName,
                                @RequestParam String productDescription,
                                @RequestParam double productPrice,
                                @RequestParam MultipartFile productImage) {
        Product newProduct = new Product(productName, productDescription, productPrice, productImage.getOriginalFilename());
        productService.saveProduct(newProduct, productImage);
        return "redirect:/"; // Redirect to homepage
    }
    @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable("id") Long productId, Model model) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return "error/404"; // Optional: custom error page
        }
        model.addAttribute("product", product);
        return "product-detail"; // refers to product-detail.html in templates folder
    }
}
