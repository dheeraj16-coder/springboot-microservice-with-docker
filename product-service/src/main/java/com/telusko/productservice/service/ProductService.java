package com.telusko.productservice.service;

import com.telusko.productservice.model.Product;
import com.telusko.productservice.dao.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepo productRepo;

    // 1. Get All Products
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            return new ResponseEntity<>(productRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    // 2. Get Products by Category
    public ResponseEntity<List<Product>> getProductsByCategory(String category) {
        try {
            return new ResponseEntity<>(productRepo.findByCategory(category), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    // 3. Add a New Product
    public ResponseEntity<String> addProduct(Product product) {
        productRepo.save(product);
        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }

    // 4. Get Single Product (CRITICAL for Order Service)
    // The Order Service calls this to get Price and Name
    public ResponseEntity<Product> getProductById(Integer id) {
        Optional<Product> product = productRepo.findById(id);
        if(product.isPresent()){
            return new ResponseEntity<>(product.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    // 5. Reduce Stock (New E-commerce Logic)
    // Call this after an order is placed
    public ResponseEntity<String> reduceStock(Integer id, Integer quantity) {
        Optional<Product> productOpt = productRepo.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepo.save(product);
                return new ResponseEntity<>("Stock updated", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Insufficient stock", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
    }
}