package com.example.apros.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.example.apros.entity.AdminAccount;
import com.example.apros.repository.AdminAccountRepository;
import com.example.apros.service.AdminAuthService;

/** 初回のみ環境変数を使って管理者を作成する。コードにパスワードを埋め込まない。 */
@Component
public class AdminFirstUserSetup implements ApplicationRunner {
    private final AdminAccountRepository repository;
    private final AdminAuthService authService;

    @Value("${APROS_ADMIN_SETUP_USERNAME:}")
    private String setupUsername;

    @Value("${APROS_ADMIN_SETUP_PASSWORD:}")
    private String setupPassword;

    public AdminFirstUserSetup(AdminAccountRepository repository, AdminAuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 設定がない場合や管理者が存在する場合は何もしない。
        if (repository.count() != 0 || (setupUsername.isBlank() && setupPassword.isBlank())) return;
        String username = setupUsername.trim();
        if (username.isBlank() || username.length() > 80 || setupPassword.length() < 12) {
            throw new IllegalStateException(
                    "初期管理者はユーザー名80文字以内、パスワード12文字以上で設定してください");
        }
        AdminAccount account = new AdminAccount();
        account.setUsername(username);
        account.setPasswordHash(authService.hashPassword(setupPassword));
        account.setEnabled(true);
        repository.save(account);
    }
}
