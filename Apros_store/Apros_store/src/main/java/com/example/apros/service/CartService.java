package com.example.apros.service;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import com.example.apros.entity.Products;
import com.example.apros.repository.ProductsRepository;

@Service
public class CartService {
    private final ProductsRepository productsRepository;

    public CartService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    // セッションには商品IDと数量だけを保持する
    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getCart(HttpSession session) {
        return (Map<Integer, Integer>) session.getAttribute("cart");
    }

    public boolean isEmpty(HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        return cart == null || cart.isEmpty();
    }

    public boolean add(Integer productId, HttpSession session) {
        Products product = productsRepository.findById(productId).orElse(null);
        if (product == null || Integer.valueOf(9999).equals(product.getCategoryId())) return false;
        Map<Integer, Integer> cart = getCart(session);
        if (cart == null) cart = new LinkedHashMap<>();
        int quantity = cart.getOrDefault(productId, 0);
        if (product.getStock() == null || quantity >= product.getStock()) return false;
        cart.put(productId, quantity + 1);
        session.setAttribute("cart", cart);
        return true;
    }

    public void delete(Integer productId, HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        if (cart == null) return;
        cart.remove(productId);
        session.setAttribute("cart", cart);
    }

    public void changeQuantity(Integer productId, int change, HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        if (cart == null || !cart.containsKey(productId) || (change != 1 && change != -1)) return;
        Products product = productsRepository.findById(productId).orElse(null);
        int newQuantity = cart.get(productId) + change;
        if (newQuantity <= 0) {
            cart.remove(productId);
        } else if (change < 0 || (product != null
                && !Integer.valueOf(9999).equals(product.getCategoryId())
                && product.getStock() != null && newQuantity <= product.getStock())) {
            cart.put(productId, newQuantity);
        }
        session.setAttribute("cart", cart);
    }

    // 画面表示のたびにDBから最新の商品情報を取得。商品オブジェクトはセッションに保存しない
    public Map<Products, Integer> getCartItems(HttpSession session) {
        Map<Products, Integer> items = new LinkedHashMap<>();
        Map<Integer, Integer> cart = getCart(session);
        if (cart == null) return items;
        java.util.Iterator<Map.Entry<Integer, Integer>> iterator = cart.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            Products product = productsRepository.findById(entry.getKey()).orElse(null);
            if (product != null && entry.getValue() != null && entry.getValue() > 0) {
                items.put(product, entry.getValue());
            } else {
                iterator.remove();
            }
        }
        session.setAttribute("cart", cart);
        return items;
    }

    public int calculateTotalPrice(Map<Products, Integer> items) {
        int total = 0;
        for (Map.Entry<Products, Integer> entry : items.entrySet()) {
            if (entry.getKey().getPrice() != null) {
                total += entry.getKey().getPrice() * entry.getValue();
            }
        }
        return total;
    }

    public int calculateCartCount(Map<Products, Integer> items) {
        int count = 0;
        for (Integer quantity : items.values()) count += quantity;
        return count;
    }
}
