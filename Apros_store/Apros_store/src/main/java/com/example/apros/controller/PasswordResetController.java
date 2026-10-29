package com.example.apros.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.apros.form.PasswordResetForm;
import com.example.apros.service.UserService;

@Controller
public class PasswordResetController {
    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/password-reset")
    public String showPasswordReset(Model model) {
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        return "password-reset";
    }

    @PostMapping("/password-reset")
    public String resetPassword(
            @ModelAttribute("passwordResetForm") PasswordResetForm form,
            Model model) {

        Map<String, String> errors = userService.validatePasswordReset(
                form.getEmail(), form.getNewPassword(), form.getConfirmPassword());

        if (!errors.isEmpty()) {
            errors.forEach(model::addAttribute);
            return "password-reset";
        }

        boolean changed = userService.changePassword(form.getEmail(), form.getNewPassword());
        if (!changed) {
            model.addAttribute("emailError", "入力されたメールアドレスに該当するユーザーが見つかりません");
            return "password-reset";
        }

        return "redirect:/login?passwordChanged";
    }
}
