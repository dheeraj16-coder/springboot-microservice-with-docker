package com.telusko.orderservice.controller;

import com.telusko.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("placeOrder")
    public String placeOrder(@RequestParam Integer productId, @RequestParam Integer quantity) {
        return orderService.placeOrder(productId, quantity);
    }
}