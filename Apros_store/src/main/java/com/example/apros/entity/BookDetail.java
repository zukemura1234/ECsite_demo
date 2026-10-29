package com.example.apros.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="book_detail")
public class BookDetail {
	@Id @Column(name="product_id")
	private Integer productId;
	private String author;
	private String publisher;
	@Column(name="issue_date")
	private Date issueDate;
	
	public Integer getProductId() {
		return productId;
	}
	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public Date getIssueDate() {
		return issueDate;
	}
	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}
}