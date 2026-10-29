package com.example.apros.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import com.example.apros.entity.AdminAccount;
import com.example.apros.repository.AdminAccountRepository;
import com.example.apros.service.AdminAuthService;

/**
 * 起動時に環境変数から管理者アカウントを作成する。何度起動しても同じ結果になる（冪等）。
 * コードにパスワードを埋め込まない。
 *
 * 1) 編集アカウント : APROS_ADMIN_SETUP_USERNAME / APROS_ADMIN_SETUP_PASSWORD
 *                     管理者が1人もいないときだけ作成する。
 * 2) 参照専用アカウント: APROS_ADMIN_VIEWER_USERNAME / APROS_ADMIN_VIEWER_PASSWORD
 *                     編集アカウント(apros.admin.editor-id)が存在し、同名のユーザーがいないときだけ作成する。
 */
@Component
public class AdminFirstUserSetup implements ApplicationRunner {
    private final AdminAccountRepository repository;
    private final AdminAuthService authService;
    private final long editorAccountId;

    @Value("${APROS_ADMIN_SETUP_USERNAME:}")
    private String setupUsername;

    @Value("${APROS_ADMIN_SETUP_PASSWORD:}")
    private String setupPassword;

    @Value("${APROS_ADMIN_VIEWER_USERNAME:}")
    private String viewerUsername;

    @Value("${APROS_ADMIN_VIEWER_PASSWORD:}")
    private String viewerPassword;

    public AdminFirstUserSetup(AdminAccountRepository repository, AdminAuthService authService,
                               @Value("${apros.admin.editor-id:1}") long editorAccountId) {
        this.repository = repository;
        this.authService = authService;
        this.editorAccountId = editorAccountId;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 先に編集アカウントを作る。参照専用を先に作ると、最初のID(=編集権限のID)を取ってしまうため
        if (repository.count() == 0 && !(setupUsername.isBlank() && setupPassword.isBlank())) {
            create(setupUsername, setupPassword);
        }
        if (!(viewerUsername.isBlank() && viewerPassword.isBlank())) {
            // 編集アカウントが存在しない状態では作らない（新しいアカウントに編集権限が付くのを防ぐ）
            if (repository.findById(editorAccountId).isEmpty()) return;
            create(viewerUsername, viewerPassword);
        }
    }

    private void create(String rawUsername, String rawPassword) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        if (username.isBlank() || username.length() > 80 || rawPassword == null || rawPassword.length() < 12) {
            throw new IllegalStateException(
                    "管理者はユーザー名80文字以内、パスワード12文字以上で設定してください");
        }
        // 同名のアカウントが既にあれば何もしない（再起動しても重複エラーにならない）
        if (repository.findByUsername(username).isPresent()) return;

        AdminAccount account = new AdminAccount();
        account.setUsername(username);
        account.setPasswordHash(authService.hashPassword(rawPassword));
        account.setEnabled(true);
        try {
            repository.save(account);
        } catch (DataIntegrityViolationException e) {
            // 複数のアプリを同時に起動して、他方が先に作成した場合は無視する
        }
    }
}
