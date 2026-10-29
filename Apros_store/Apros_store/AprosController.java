package com.example.apros;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AprosController {
	@Autowired private ProductsRepository repository;
	@Autowired private UsersRepository usersRepository;
	
	@GetMapping("/")
	public String index(Model model, HttpSession session) {

	    List<Products> products_list = repository.findAll();
	    model.addAttribute("products_list", products_list);

	    // Sessionからログイン情報を取得
	    Object userId = session.getAttribute("userId");

	    // HTMLにログイン情報を渡す
	    model.addAttribute("userId", userId);

	    return "index";
	}
	
	@GetMapping("/login")
	public String showLogin(Model model) {
		model.addAttribute("users", new Users());
	    return "login";
	}
	
	@PostMapping("/login")
	public String methodLogin(
		@Validated @ModelAttribute("users") Users users,
		BindingResult result,
		HttpServletRequest request) {

	    // ① 入力チェック
		if (result.hasErrors()) {
			return "login";
		}

	    // ② ユーザー検索
		Users loginUser = usersRepository
				.findByEmail(users.getEmail())
				.orElse(null);

	    // ③ パスワード確認
		if (loginUser == null ||
				!users.getPassword().equals(loginUser.getPassword())) {

			result.reject(
					"loginError",
					"メールアドレスまたはパスワードが違います"
			);
			return "login";
		}

		// ④ セッションに保存
		HttpSession session = request.getSession();
		request.changeSessionId();

		session.setAttribute("userId", loginUser.getId());

		// ⑤ 商品一覧画面に移動
		return "redirect:/";
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {

	    // セッションを無効化する
	    session.invalidate();

	    // 商品一覧画面に戻る
	    return "redirect:/";
	}
}
