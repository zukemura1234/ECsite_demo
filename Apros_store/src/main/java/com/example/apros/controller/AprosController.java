package com.example.apros.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.apros.entity.Users;
import com.example.apros.repository.ProductsRepository;
import com.example.apros.service.UserService;

@Controller
public class AprosController {
    private final ProductsRepository productsRepository;
    private final UserService userService;

    public AprosController(ProductsRepository productsRepository, UserService userService) {
        this.productsRepository = productsRepository;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("products_list", productsRepository.findAll());
        model.addAttribute("userId", session.getAttribute("userId"));
        return "index";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("users", new Users());
        return "login";
    }

    @PostMapping("/login")
    public String methodLogin(@Validated @ModelAttribute("users") Users users,
            BindingResult result, HttpServletRequest request,
            @RequestParam(value = "next", defaultValue = "") String next) {
        if (result.hasErrors()) return "login";
        Users loginUser = userService.authenticate(users.getEmail(), users.getPassword());
        if (loginUser == null) {
            result.reject("loginError", "メールアドレスまたはパスワードが違います");
            return "login";
        }
        // パスワードが正しい場合だけ表示する（メールアドレスの存在を第三者に知られないようにするため）
        if (!loginUser.isActive()) {
            result.reject("loginError", "このアカウントは現在ご利用いただけません。お問い合わせください。");
            return "login";
        }
        HttpSession session = request.getSession();
        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (!loginUser.getId().equals(currentUserId)) {
            if (currentUserId != null) session.removeAttribute("cart");
            for (String key : new String[]{"Name", "PostalCode", "Prefecture", "City",
                    "AddressLine", "Building", "PaymentMethod", "ShippingType", "Token"}) {
                session.removeAttribute("checkout" + key);
            }
            session.removeAttribute("lastOrder");
            session.removeAttribute("lastOrderToken");
        }
        request.changeSessionId();
        session.setAttribute("userId", loginUser.getId());
        return "checkout".equals(next) ? "redirect:/checkout" : "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("user") Users user, Model model,
            @RequestParam(value = "next", defaultValue = "") String next) {
        Map<String, String> errors = userService.validateRegistration(user);
        if (!errors.isEmpty()) {
            errors.forEach(model::addAttribute);
            return "register";
        }
        userService.register(user);
        return "checkout".equals(next) ? "redirect:/login?registered&next=checkout" : "redirect:/login?registered";
    }
}
