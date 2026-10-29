package com.example.apros.service;

import java.time.format.DateTimeFormatter;

import com.example.apros.entity.Users;

/** 管理者画面に表示するユーザー情報。パスワード（ハッシュ）は含めない。 */
public class AdminUserRow {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final Integer id;
    private final String name;
    private final String email;
    private final String address;
    private final String createdAt;
    private final boolean active;

    private AdminUserRow(Integer id, String name, String email, String address,
                         String createdAt, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
        this.active = active;
    }

    public static AdminUserRow from(Users u) {
        StringBuilder address = new StringBuilder();
        if (u.getPostalCode() != null && !u.getPostalCode().isBlank()) {
            address.append("〒").append(u.getPostalCode()).append(' ');
        }
        for (String part : new String[]{u.getPrefecture(), u.getCity(), u.getAddressLine(), u.getBuilding()}) {
            if (part != null && !part.isBlank()) address.append(part);
        }
        return new AdminUserRow(u.getId(), u.getName(), u.getEmail(), address.toString(),
                u.getCreatedAt() == null ? "—" : u.getCreatedAt().format(FORMAT), u.isActive());
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
}
