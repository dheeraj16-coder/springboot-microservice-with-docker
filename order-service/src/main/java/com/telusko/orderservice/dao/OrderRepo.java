package com.telusko.orderservice.dao;

import com.telusko.orderservice.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Orders, Integer> {
    // You can add custom queries here later if needed
}