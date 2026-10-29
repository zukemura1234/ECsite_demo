package com.example.apros.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.apros.entity.Users;
import com.example.apros.repository.UsersRepository;

@Service
public class UserService {
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    
    public UserService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users authenticate(String email, String password) {
        Users user = usersRepository.findByEmail(email).orElse(null);
        if (user == null || password == null) {
            return null;
        }

        // 入力パスワードとDBのハッシュ値を照合
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public Users findById(Integer userId) {
        return userId == null ? null : usersRepository.findById(userId).orElse(null);
    }

    public Map<String, String> validateRegistration(Users user) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(user.getName())) errors.put("nameError", "名前を入力してください");
        if (blank(user.getEmail())) {
            errors.put("emailError", "メールアドレスを入力してください");
        } else if (!user.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.put("emailError", "メールアドレスの形式が正しくありません");
        }
        if (blank(user.getPassword())) {
            errors.put("passwordError", "パスワードを入力してください");
        } else if (user.getPassword().length() < 8) {
            errors.put("passwordError", "パスワードは8文字以上で入力してください");
        }
        if (user.getPostalCode() == null || !user.getPostalCode().matches("^[0-9]{3}-?[0-9]{4}$")) {
            errors.put("postalCodeError", "郵便番号を正しく入力してください");
        }
        if (blank(user.getPrefecture())) errors.put("prefectureError", "都道府県を入力してください");
        if (blank(user.getCity())) errors.put("cityError", "市区町村を入力してください");
        if (blank(user.getAddressLine())) errors.put("addressLineError", "住所を入力してください");
        return errors;
    }

    public void register(Users user) {
        user.setPostalCode(user.getPostalCode().replace("-", ""));
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true); // フォームから値が送られても無視して、必ずアクティブで開始する
        usersRepository.save(user);
    }

    /**
     * メールアドレスが、ログイン中のユーザー以外に使用されているか確認する。
     */
    public boolean emailInUseByOtherUser(String email, Integer userId) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return usersRepository.findByEmail(email.trim())
                .map(user -> !user.getId().equals(userId))
                .orElse(false);
    }

    /**
     * 既存ユーザーの編集済み情報を保存する。
     * 呼び出し元で、更新を許可する項目だけを既存エンティティへ反映してから呼ぶ。
     */
    public void updateProfile(Users existing) {
        if (existing == null || existing.getId() == null) {
            throw new IllegalArgumentException("更新対象のユーザーが指定されていません。");
        }

        usersRepository.save(existing);
    }
    
    /**
     * パスワード変更時の入力チェック
     */
    public Map<String, String> validatePasswordReset(
            String email,
            String newPassword,
            String confirmPassword) {

        Map<String, String> errors = new LinkedHashMap<>();

        if (blank(email)) {
            errors.put("emailError",
                    "メールアドレスを入力してください");
        } else if (!email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.put("emailError",
                    "メールアドレスの形式が正しくありません");
        }

        if (blank(newPassword)) {
            errors.put("passwordError",
                    "新しいパスワードを入力してください");
        } else if (newPassword.length() < 8) {
            errors.put("passwordError",
                    "パスワードは8文字以上で入力してください");
        }

        if (blank(confirmPassword)) {
            errors.put("confirmPasswordError",
                    "確認用パスワードを入力してください");
        } else if (newPassword != null
                && !newPassword.equals(confirmPassword)) {
            errors.put("confirmPasswordError",
                    "パスワードが一致しません");
        }

        return errors;
    }

    /**
     * メールアドレスに該当するユーザーの
     * パスワードを変更する
     */
    public boolean changePassword(
            String email, String newPassword) {

        if (email == null || newPassword == null) {
            return false;
        }

        Users user = usersRepository
                .findByEmail(email.trim())
                .orElse(null);

        if (user == null) {
            return false;
        }

        // 既存のログイン認証と同じ BCrypt でハッシュ化
        user.setPassword(passwordEncoder.encode(newPassword));

        // データベースを更新
        usersRepository.save(user);

        return true;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
