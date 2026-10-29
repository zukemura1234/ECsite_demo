package com.example.apros.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.apros.entity.AdminAccount;
import com.example.apros.repository.AdminAccountRepository;

@Service
public class AdminAuthService {
    private final AdminAccountRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 商品情報を編集できる管理者アカウントのID（admin_accounts.id）。1つだけ指定する。 */
    private final long editorAccountId;

    public AdminAuthService(AdminAccountRepository repository,
                            @Value("${apros.admin.editor-id:1}") long editorAccountId) {
        this.repository = repository;
        this.editorAccountId = editorAccountId;
    }

    public Optional<AdminAccount> authenticate(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return repository.findByUsername(username.trim())
                .filter(AdminAccount::isEnabled)
                .filter(account -> encoder.matches(password, account.getPasswordHash()));
    }

    public boolean isValidAdmin(Object adminId) {
        if (!(adminId instanceof Long)) return false;
        return repository.findById((Long) adminId)
                .map(AdminAccount::isEnabled)
                .orElse(false);
    }

    /** 有効な管理者で、かつ編集権限を持つ唯一のアカウントか。 */
    public boolean canEdit(Object adminId) {
        return adminId instanceof Long id
                && id.longValue() == editorAccountId
                && isValidAdmin(id);
    }

    /** ログイン後の移動先。編集権限がなければ参照専用ページへ。 */
    public String homePath(Object adminId) {
        return canEdit(adminId) ? "/admin/products" : "/admin/view/products";
    }

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
