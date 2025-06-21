package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public void saveProduct(Product product, MultipartFile productImage) {
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        Path uploadPath = Paths.get(uploadDir);

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String imageExtension = getFileExtension(productImage.getOriginalFilename());
            String imageName = UUID.randomUUID().toString() + imageExtension;
            Path imagePath = uploadPath.resolve(imageName);

            Files.write(imagePath, productImage.getBytes());

            product.setImageName(imageName);
            productRepository.save(product);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Fix this method
    public Product getProductById(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        return optionalProduct.orElse(null); // return null if not found
    }
}
