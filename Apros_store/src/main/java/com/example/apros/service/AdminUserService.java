package com.example.apros.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apros.repository.UsersRepository;

@Service
public class AdminUserService {
    private final UsersRepository usersRepository;

    public AdminUserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserRow> search(String keyword, Boolean active, Pageable pageable) {
        return usersRepository.searchForAdmin(keyword, active, pageable).map(AdminUserRow::from);
    }

    @Transactional(readOnly = true)
    public long countAll() { return usersRepository.count(); }

    @Transactional(readOnly = true)
    public long countActive(boolean active) { return usersRepository.countByActive(active); }

    /** アクティブ状態を変更する。呼び出し側（編集アカウント限定）で権限確認済みであること。 */
    @Transactional
    public boolean setActive(Integer id, boolean active) {
        if (id == null || !usersRepository.existsById(id)) return false;
        usersRepository.updateActive(id, active);
        return true;
    }
}
