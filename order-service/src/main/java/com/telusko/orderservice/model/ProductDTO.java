package com.telusko.orderservice.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Integer id;
    private String name;
    private Double price;
    private String description;
    private String category;
    private Integer stockQuantity;
}