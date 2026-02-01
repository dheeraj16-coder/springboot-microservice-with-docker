package com.telusko.productservice.dao;

import com.telusko.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    // JPA automatically generates the SQL for this based on the method name
    // SELECT * FROM product WHERE category = ?
    List<Product> findByCategory(String category);
}