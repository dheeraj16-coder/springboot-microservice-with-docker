package com.telusko.orderservice.service;

import com.telusko.orderservice.dao.OrderRepo;
import com.telusko.orderservice.dto.ProductDTO;
import com.telusko.orderservice.feign.ProductInterface;
import com.telusko.orderservice.model.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ProductInterface productInterface;

    public String placeOrder(Integer productId, Integer quantity) {
        // 1. Get Product Details
        ProductDTO product = productInterface.getProductById(productId).getBody();

        if (product != null) {
            // 2. Create Order
            Orders order = new Orders();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setSkuCode(product.getName());
            order.setPrice(product.getPrice());
            order.setQuantity(quantity);
            order.setTotalAmount(product.getPrice() * quantity);

            // 3. Save Order
            orderRepo.save(order);

            // 4. Update Stock in Product Service
            productInterface.reduceStock(productId, quantity);

            return "Order Placed Successfully! Order ID: " + order.getOrderNumber();
        } else {
            return "Product not found or Out of Stock";
        }
    }
}