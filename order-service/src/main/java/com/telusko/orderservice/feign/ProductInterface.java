package com.telusko.orderservice.feign;

import com.telusko.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("PRODUCT-SERVICE") // Must match Product Service application name
public interface ProductInterface {

    // Get product details
    @GetMapping("product/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable("id") Integer id);

    // Reduce stock after ordering
    @PostMapping("product/reduceStock")
    public ResponseEntity<String> reduceStock(@RequestParam Integer id, @RequestParam Integer quantity);
}