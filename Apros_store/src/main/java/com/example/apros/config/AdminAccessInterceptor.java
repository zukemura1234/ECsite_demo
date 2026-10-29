package com.example.apros.config;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.apros.service.AdminAuthService;

/** 管理者URLを直接入力されてもログイン・CSRF確認を必ず行う。 */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {
    private final AdminAuthService authService;

    public AdminAccessInterceptor(AdminAuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("/admin/login".equals(path)) return true;

        HttpSession session = request.getSession(false);
        if (session == null || !authService.isValidAdmin(session.getAttribute("adminAccountId"))) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
        }
        // 編集権限の確認：@AdminViewAllowed が付いていないハンドラーは編集アカウント専用
        if (handler instanceof HandlerMethod hm && !isViewAllowed(hm)
                && !authService.canEdit(session.getAttribute("adminAccountId"))) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                response.sendRedirect(request.getContextPath() + "/admin/view/products");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "この操作を行う権限がありません");
            }
            return false;
        }
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Object expected = session.getAttribute("adminCsrf");
            String supplied = request.getParameter("adminCsrf");
            if (!(expected instanceof String) || !expected.equals(supplied)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "不正なリクエストです");
                return false;
            }
        }
        return true;
    }

    private boolean isViewAllowed(HandlerMethod handler) {
        return handler.hasMethodAnnotation(AdminViewAllowed.class)
                || AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), AdminViewAllowed.class);
    }
}
