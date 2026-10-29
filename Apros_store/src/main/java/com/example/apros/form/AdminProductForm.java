package com.example.apros.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** products テーブルの編集可能な列だけを受け取る。 */
public class AdminProductForm {
    @NotNull(message = "商品IDを入力してください")
    @Min(value = 1, message = "商品IDは1以上にしてください")
    private Integer id;

    @NotBlank(message = "商品名を入力してください")
    @Size(max = 100, message = "商品名は100文字以内にしてください")
    private String name;

    @NotNull(message = "価格を入力してください")
    @Min(value = 0, message = "価格は0円以上にしてください")
    private Integer price;

    @NotNull(message = "在庫数を入力してください")
    @Min(value = 0, message = "在庫数は0以上にしてください")
    private Integer stock;

    @NotNull(message = "カテゴリーを選択してください")
    private Integer categoryId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}
