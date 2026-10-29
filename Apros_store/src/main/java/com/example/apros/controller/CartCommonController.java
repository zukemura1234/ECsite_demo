package com.example.apros.controller;

import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.apros.entity.Products;
import com.example.apros.service.CartService;

@ControllerAdvice
public class CartCommonController {
    private final CartService cartService;

    public CartCommonController(CartService cartService) {
        this.cartService = cartService;
    }

    // 共通のカート情報を各画面のModelへ。リクエストごとにDBを参照する
    @ModelAttribute
    public void addCartAttributes(HttpSession session, Model model) {
        Map<Products, Integer> items = cartService.getCartItems(session);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartCount", cartService.calculateCartCount(items));
        model.addAttribute("totalPrice", cartService.calculateTotalPrice(items));
    }
}
