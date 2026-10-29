package com.example.apros.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.apros.repository.UsersRepository;

/**
 * ログイン中のユーザーが非アクティブにされた（または削除された）場合、
 * 次のリクエストでログイン状態を解除する。
 * 画面ごとの判定漏れを防ぐため、全ての顧客向けリクエストで確認する。
 */
@Component
public class CustomerActiveInterceptor implements HandlerInterceptor {
    private final UsersRepository usersRepository;

    public CustomerActiveInterceptor(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) return true;

        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer id && !usersRepository.existsByIdAndActive(id, true)) {
            session.removeAttribute("userId"); // ログアウト状態にする（カートは残す）
        }
        return true;
    }
}
