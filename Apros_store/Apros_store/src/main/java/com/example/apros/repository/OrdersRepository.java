package com.example.apros.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apros.entity.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUserIdOrderByOrderedAtDesc(Integer userId);
}
