package com.example.apros.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.apros.config.AdminViewAllowed;
import com.example.apros.service.AdminUserRow;
import com.example.apros.service.AdminUserService;

@Controller
public class AdminUserController {
    private final AdminUserService userService;

    public AdminUserController(AdminUserService userService) {
        this.userService = userService;
    }

    // 編集アカウント用（アクティブ状態の変更ボタンあり）。@AdminViewAllowed を付けないので編集専用
    @GetMapping("/admin/users")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "all") String status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        return render(q, status, page, model, true);
    }

    // 参照専用アカウント用。レイアウトは同じで、変更ボタンだけ表示しない
    @AdminViewAllowed
    @GetMapping("/admin/view/users")
    public String viewList(@RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "all") String status,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        return render(q, status, page, model, false);
    }

    // アクティブ状態の変更（編集アカウント専用。Interceptorで権限・CSRFを確認済み）
    @PostMapping("/admin/users/{id}/active")
    public String updateActive(@PathVariable Integer id,
                               @RequestParam int active,
                               @RequestParam(defaultValue = "") String q,
                               @RequestParam(defaultValue = "all") String status,
                               @RequestParam(defaultValue = "0") int page,
                               RedirectAttributes redirect) {
        if (active != 0 && active != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "値は0か1で指定してください");
        }
        if (!userService.setActive(id, active == 1)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーがいません");
        }
        redirect.addFlashAttribute("success",
                "ユーザーID " + id + " を" + (active == 1 ? "アクティブ" : "非アクティブ") + "に変更しました");
        // 変更後も、元の検索条件・ページに戻す
        redirect.addAttribute("q", q);
        redirect.addAttribute("status", status);
        redirect.addAttribute("page", page);
        return "redirect:/admin/users";
    }

    private String render(String q, String status, int page, Model model, boolean canEdit) {
        String keyword = q.trim();
        if (keyword.length() > 100) keyword = keyword.substring(0, 100);
        Boolean activeFilter = "1".equals(status) ? Boolean.TRUE : "0".equals(status) ? Boolean.FALSE : null;
        Pageable pageable = PageRequest.of(Math.max(0, Math.min(page, 10000)), 20, Sort.by("id").ascending());
        Page<AdminUserRow> users = userService.search(keyword, activeFilter, pageable);

        long activeCount = userService.countActive(true);
        model.addAttribute("users", users);
        model.addAttribute("q", keyword);
        model.addAttribute("status", activeFilter == null ? "all" : (activeFilter ? "1" : "0"));
        model.addAttribute("totalCount", userService.countAll());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", userService.countActive(false));
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("basePath", canEdit ? "/admin/users" : "/admin/view/users");
        return "admin/users";
    }
}
