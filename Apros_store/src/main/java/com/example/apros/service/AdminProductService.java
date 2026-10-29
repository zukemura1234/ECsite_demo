package com.example.apros.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.apros.entity.Products;
import com.example.apros.form.AdminProductForm;
import com.example.apros.repository.ProductsRepository;

@Service
public class AdminProductService {
    private final ProductsRepository productsRepository;

    public AdminProductService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @Transactional
    public boolean update(Integer id, AdminProductForm form) {
        validateCategory(form.getCategoryId());
        // 注文処理・キャンセル処理と同じ行ロックを取得して競合を避ける
        Products product = productsRepository.findByIdForUpdate(id).orElse(null);
        if (product == null) return false;
        apply(product, form);
        productsRepository.save(product);
        return true;
    }

    @Transactional
    public boolean create(AdminProductForm form) {
        validateCategory(form.getCategoryId());
        if (productsRepository.existsById(form.getId())) return false;
        Products product = new Products();
        product.setId(form.getId());
        apply(product, form);
        productsRepository.saveAndFlush(product);
        return true;
    }

    private void apply(Products product, AdminProductForm form) {
        product.setName(form.getName().trim());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setCategoryId(form.getCategoryId());
    }

    private void validateCategory(Integer categoryId) {
        if (categoryId == null || (categoryId != 1 && categoryId != 2
                && categoryId != 3 && categoryId != 4 && categoryId != 9999)) {
            throw new IllegalArgumentException("カテゴリーが不正です");
        }
    }
}
