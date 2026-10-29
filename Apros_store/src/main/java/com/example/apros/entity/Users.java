package com.example.apros.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="users")
public class Users {
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Integer id;
	private String name;
	@NotNull
	private String email;
	@NotNull
	private String password;
	@Column(name="postal_code")
	private String postalCode;
	private String prefecture;
	private String city;
	@Column(name="address_line")
	private String addressLine;
	private String building;
	@Column(name="created_at")
	private LocalDateTime createdAt;
	// 1=アクティブ(デフォルト) / 0=非アクティブ。変更できるのは管理者画面の編集アカウントのみ
	@Column(name="is_active", nullable=false)
	private boolean active = true;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getPrefecture() {
		return prefecture;
	}
	public void setPrefecture(String prefecture) {
		this.prefecture = prefecture;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAddressLine() {
		return addressLine;
	}
	public void setAddressLine(String addressLine) {
		this.addressLine = addressLine;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	
}
