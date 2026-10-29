package com.example.apros.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.apros.service.CartService;

@Controller
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/cart/add")
    @ResponseBody
    public int addCart(
            @RequestParam("productId") Integer productId,
            HttpSession session) {

        cartService.add(productId, session);

        return cartService.calculateCartCount(
                cartService.getCartItems(session));
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        // 共通のカート情報は CartCommonController が Model に設定する
        return "cart";
    }

    @PostMapping("/cart/delete")
    public String deleteCart(@RequestParam("productId") Integer productId, HttpSession session) {
        cartService.delete(productId, session);
        return "redirect:/cart";
    }

    @PostMapping("/cart/quantity")
    public String changeQuantity(@RequestParam("productId") Integer productId,
            @RequestParam("change") int change,
            @RequestParam(value = "returnTo", defaultValue = "cart") String returnTo,
            HttpSession session) {
        cartService.changeQuantity(productId, change, session);
        return "checkout".equals(returnTo) ? "redirect:/checkout" : "redirect:/cart";
    }
}
