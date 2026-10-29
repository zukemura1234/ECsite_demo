package com.example.apros.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="food_detail")
public class FoodDetail {
	@Id @Column(name="product_id")
	private Integer productId;
	@Column(name="expiration_date")
	private Date expirationDate;
	@Column(name="storage_method")
	private String storageMethod;
	private String allergens;
	@Column(name="content_volume")
	private String contentVolume;
	
	public Integer getProductId() {
		return productId;
	}
	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public String getStorageMethod() {
		return storageMethod;
	}
	public void setStorageMethod(String storageMethod) {
		this.storageMethod = storageMethod;
	}
	public String getAllergens() {
		return allergens;
	}
	public void setAllergens(String allergens) {
		this.allergens = allergens;
	}
	public String getContentVolume() {
		return contentVolume;
	}
	public void setContentVolume(String contentVolume) {
		this.contentVolume = contentVolume;
	}
}
