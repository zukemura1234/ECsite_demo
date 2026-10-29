package com.example.apros.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.apros.entity.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByUserIdOrderByOrderedAtDesc(Integer userId);

    // 二重キャンセルを防止するため注文行を排他ロック
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orders o WHERE o.id = :id")
    Optional<Orders> findByIdForUpdate(@Param("id") Integer id);
}
