package com.example.apros.service;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.apros.entity.AdminAccount;
import com.example.apros.repository.AdminAccountRepository;

@Service
public class AdminAuthService {
    private final AdminAccountRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AdminAuthService(AdminAccountRepository repository) {
        this.repository = repository;
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

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
