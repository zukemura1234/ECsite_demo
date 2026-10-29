package com.example.apros.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.apros.entity.Products;
import com.example.apros.repository.AparelDetailRepository;
import com.example.apros.repository.BookDetailRepository;
import com.example.apros.repository.ElectDetailRepository;
import com.example.apros.repository.FoodDetailRepository;
import com.example.apros.repository.ProductsRepository;

@Controller
public class ProductDetailController {
	private final ProductsRepository productsRepository;
	private final ElectDetailRepository electDetailRepository;
	private final AparelDetailRepository aparelDetailRepository;
	private final FoodDetailRepository foodDetailRepository;
	private final BookDetailRepository bookDetailRepository;
	
	public ProductDetailController(
			ProductsRepository productsRepository,
			ElectDetailRepository electDetailRepository,
			AparelDetailRepository aparelDetailRepository,
			FoodDetailRepository foodDetailRepository,
			BookDetailRepository bookDetailRepository) {
		
		this.productsRepository = productsRepository;
		this.electDetailRepository = electDetailRepository;
		this.aparelDetailRepository = aparelDetailRepository;
		this.foodDetailRepository = foodDetailRepository;
		this.bookDetailRepository = bookDetailRepository;
	}
	
	//商品詳細ページ
	@GetMapping("/products/{id}")
	public String productDetail(
		@PathVariable Integer id,
		Model model) {
		Products product = productsRepository.findById(id).orElse(null);
		if (product == null) return "redirect:/";

		//カテゴリーごとのdetail
		Object detail = null;
		switch (product.getCategoryId()) {
		case 1:
			detail = electDetailRepository.findById(id).orElse(null);
			break;
		case 2:
			detail = aparelDetailRepository.findById(id).orElse(null);
			break;
		case 3:
			detail = foodDetailRepository.findById(id).orElse(null);
			break;
		case 4:
			detail = bookDetailRepository.findById(id).orElse(null);
			break;
		}
		
		model.addAttribute("product", product);
		model.addAttribute("detail", detail);
		return "product-detail";
	}
}
