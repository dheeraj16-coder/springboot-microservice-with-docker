package com.telusko.productservice.controller;

import com.telusko.productservice.model.Product;
import com.telusko.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return productService.getAllProducts();
    }


    @GetMapping("{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id);
    }

    @GetMapping("stock/{id}")
    public ResponseEntity<Integer> getStock(@PathVariable Integer id){
        ResponseEntity<Product> response = productService.getProductById(id);

        Product product = response.getBody();

        if (product != null) {
            return ResponseEntity.ok(product.getStockQuantity());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("add")
    public ResponseEntity<String> addProduct(@RequestBody Product product){
        return productService.addProduct(product);
    }

    @PostMapping("reduceStock")
    public ResponseEntity<String> reduceStock(@RequestParam Integer id, @RequestParam Integer quantity){
        return productService.reduceStock(id, quantity);
    }
}