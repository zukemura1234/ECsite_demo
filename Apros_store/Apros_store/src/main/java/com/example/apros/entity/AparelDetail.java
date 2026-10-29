package com.example.apros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="aparel_detail")
public class AparelDetail {
	@Id @Column(name="product_id")
	private Integer productId;
	private String brand;
	private String color;
	private String model;
	private String other;
	
	public Integer getProductId() {
		return productId;
	}
	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	
	
}
