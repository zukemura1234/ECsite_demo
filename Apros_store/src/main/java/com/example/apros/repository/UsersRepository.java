package com.example.apros.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.apros.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);

    // 管理者画面：名前・メールの部分一致 + アクティブ状態での絞り込み（active が null なら全件）
    @Query("""
            SELECT u FROM Users u
            WHERE (:q = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<Users> searchForAdmin(@Param("q") String q, @Param("active") Boolean active, Pageable pageable);

    long countByActive(boolean active);

    // ログイン中ユーザーが今もアクティブか（1リクエストごとの確認用。行全体は読み込まない）
    boolean existsByIdAndActive(Integer id, boolean active);

    // アクティブ状態の列だけを更新する（他の項目を上書きしない）
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Users u SET u.active = :active WHERE u.id = :id")
    int updateActive(@Param("id") Integer id, @Param("active") boolean active);
}
