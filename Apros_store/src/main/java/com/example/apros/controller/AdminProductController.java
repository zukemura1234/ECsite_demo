package com.example.apros.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.apros.config.AdminViewAllowed;
import com.example.apros.entity.Products;
import com.example.apros.form.AdminProductForm;
import com.example.apros.repository.ProductsRepository;
import com.example.apros.service.AdminAuthService;
import com.example.apros.service.AdminProductService;

@Controller
public class AdminProductController {
    private final ProductsRepository repository;
    private final AdminProductService productService;
    private final AdminAuthService authService;

    public AdminProductController(ProductsRepository repository, AdminProductService productService,
                                  AdminAuthService authService) {
        this.repository = repository;
        this.productService = productService;
        this.authService = authService;
    }

    @AdminViewAllowed
    @GetMapping("/admin")
    public String home(HttpSession session) {
        return "redirect:" + authService.homePath(session.getAttribute("adminAccountId"));
    }

    // 編集アカウント用の一覧（追加・編集ボタンあり）。@AdminViewAllowed を付けないことで編集専用になる
    @GetMapping("/admin/products")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "all") String category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        return renderList(q, category, page, model, true);
    }

    // 参照専用アカウント用の一覧。レイアウトは同じで、追加・編集の導線だけ表示しない
    @AdminViewAllowed
    @GetMapping("/admin/view/products")
    public String viewList(@RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "all") String category,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        return renderList(q, category, page, model, false);
    }

    private String renderList(String q, String category, int page, Model model, boolean canEdit) {
        String keyword = q.trim();
        if (keyword.length() > 100) keyword = keyword.substring(0, 100);
        Integer categoryId = parseCategory(category);
        Pageable pageable = PageRequest.of(Math.max(0, Math.min(page, 10000)), 20,
                Sort.by("id").ascending());
        Page<Products> products;
        if (categoryId != null) {
            products = keyword.isEmpty()
                    ? repository.findByCategoryId(categoryId, pageable)
                    : repository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable);
        } else {
            products = keyword.isEmpty()
                    ? repository.findAll(pageable)
                    : repository.findByNameContainingIgnoreCase(keyword, pageable);
        }
        model.addAttribute("products", products);
        model.addAttribute("q", keyword);
        model.addAttribute("category", categoryId == null ? "all" : categoryId.toString());
        model.addAttribute("totalCount", repository.count());
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("basePath", canEdit ? "/admin/products" : "/admin/view/products");
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String createPage(Model model) {
        AdminProductForm form = new AdminProductForm();
        form.setPrice(0);
        form.setStock(0);
        form.setCategoryId(1);
        model.addAttribute("productForm", form);
        model.addAttribute("isNew", true);
        return "admin/product-form";
    }

    @PostMapping("/admin/products/new")
    public String create(@Valid @ModelAttribute("productForm") AdminProductForm form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        validateCategory(form, errors);
        if (!errors.hasErrors() && repository.existsById(form.getId())) {
            errors.rejectValue("id", "duplicate", "その商品IDは使用されています");
        }
        if (errors.hasErrors()) {
            model.addAttribute("isNew", true);
            return "admin/product-form";
        }
        if (!productService.create(form)) {
            errors.rejectValue("id", "duplicate", "その商品IDは使用されています");
            model.addAttribute("isNew", true);
            return "admin/product-form";
        }
        redirect.addFlashAttribute("success", "商品を追加しました");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editPage(@PathVariable Integer id, Model model) {
        Products product = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "商品がありません"));
        AdminProductForm form = new AdminProductForm();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setCategoryId(product.getCategoryId());
        model.addAttribute("productForm", form);
        model.addAttribute("isNew", false);
        model.addAttribute("productId", id);
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{id}/edit")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("productForm") AdminProductForm form,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        validateCategory(form, errors);
        if (errors.hasErrors()) {
            model.addAttribute("isNew", false);
            model.addAttribute("productId", id);
            return "admin/product-form";
        }
        if (!productService.update(id, form)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品がありません");
        }
        redirect.addFlashAttribute("success", "商品ID " + id + " を更新しました");
        return "redirect:/admin/products";
    }

    private void validateCategory(AdminProductForm form, BindingResult errors) {
        Integer id = form.getCategoryId();
        if (id != null && id != 1 && id != 2 && id != 3 && id != 4 && id != 9999) {
            errors.rejectValue("categoryId", "invalid", "正しいカテゴリーを選択してください");
        }
    }

    private Integer parseCategory(String category) {
        try {
            int id = Integer.parseInt(category);
            return id == 1 || id == 2 || id == 3 || id == 4 || id == 9999 ? id : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
