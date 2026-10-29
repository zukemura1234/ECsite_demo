package com.example.apros.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apros.entity.OrderDetails;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    List<OrderDetails> findByOrderId(Integer orderId);
}
