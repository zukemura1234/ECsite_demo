package com.example.apros.controller;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.example.apros.config.AdminViewAllowed;
import com.example.apros.entity.AdminAccount;
import com.example.apros.service.AdminAuthService;

@Controller
public class AdminAuthController {
    private final AdminAuthService authService;

    public AdminAuthController(AdminAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/admin/login")
    public String loginPage(HttpSession session) {
        Object adminId = session.getAttribute("adminAccountId");
        if (authService.isValidAdmin(adminId)) {
            return "redirect:" + authService.homePath(adminId);
        }
        ensureCsrfToken(session);
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        @RequestParam(name = "adminCsrf", required = false) String token,
                        HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || token == null || !token.equals(session.getAttribute("adminCsrf"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不正なリクエストです");
        }
        AdminAccount admin = authService.authenticate(username, password).orElse(null);
        if (admin == null) {
            model.addAttribute("error", "管理者IDまたはパスワードが正しくありません");
            return "admin/login";
        }
        request.changeSessionId(); // セッション固定化対策
        session.setAttribute("adminAccountId", admin.getId());
        session.setAttribute("adminCsrf", UUID.randomUUID().toString());
        // 編集アカウントは編集画面、それ以外は参照専用画面へ
        return "redirect:" + authService.homePath(admin.getId());
    }

    @AdminViewAllowed
    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        // 顧客側の userId やカートは残し、管理者のログインだけ解除する
        session.removeAttribute("adminAccountId");
        session.setAttribute("adminCsrf", UUID.randomUUID().toString());
        return "redirect:/admin/login";
    }

    private void ensureCsrfToken(HttpSession session) {
        if (session.getAttribute("adminCsrf") == null) {
            session.setAttribute("adminCsrf", UUID.randomUUID().toString());
        }
    }
}
