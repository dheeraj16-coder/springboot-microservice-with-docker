package com.telusko.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // "order" is a reserved SQL keyword, so we use "orders"
@Data
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderNumber;
    private String skuCode;
    private Double price;
    private Integer quantity;
    private Double totalAmount;

    private LocalDateTime orderDate = LocalDateTime.now();
}