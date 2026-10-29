package com.example.apros.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.apros.entity.AdminAccount;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {
    Optional<AdminAccount> findByUsername(String username);
}
