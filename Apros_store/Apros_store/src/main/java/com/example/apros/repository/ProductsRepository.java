package com.example.apros.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.apros.entity.Products;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer>{
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Products p WHERE p.id = :id")
    Optional<Products> findByIdForUpdate(@Param("id") Integer id);
	
    // 管理者の商品一覧・検索・カテゴリー絞り込み
    Page<Products> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Products> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Products> findByCategoryIdAndNameContainingIgnoreCase(
            Integer categoryId, String name, Pageable pageable);
}

