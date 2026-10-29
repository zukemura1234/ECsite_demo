package com.example.apros.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.apros.entity.Users;
import com.example.apros.repository.OrderDetailsRepository;
import com.example.apros.repository.OrdersRepository;
import com.example.apros.repository.ProductsRepository;
import com.example.apros.service.UserService;

@Controller
public class MyPageController {

    private final UserService userService;
    private final OrdersRepository ordersRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductsRepository productsRepository;

    public MyPageController(
            UserService userService,
            OrdersRepository ordersRepository,
            OrderDetailsRepository orderDetailsRepository,
            ProductsRepository productsRepository) {
        this.userService = userService;
        this.ordersRepository = ordersRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productsRepository = productsRepository;
    }

    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        Users user = getLoginUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        // マイページにはユーザー名だけを表示する
        model.addAttribute("userName", user.getName());

        return "mypage";
    }

    @GetMapping("/mypage/profile")
    public String editProfile(HttpSession session, Model model) {
        Users user = getLoginUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        // フォームに既存情報を初期表示する
        model.addAttribute("user", user);

        return "mypage-profile";
    }

    @PostMapping("/mypage/profile")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("postalCode") String postalCode,
            @RequestParam("prefecture") String prefecture,
            @RequestParam("city") String city,
            @RequestParam("addressLine") String addressLine,
            @RequestParam(value = "building", required = false) String building,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Users existing = getLoginUser(session);

        if (existing == null) {
            return "redirect:/login";
        }

        // 入力チェック
        String normalizedEmail = email == null ? "" : email.trim();

        if (blank(name)
                || blank(normalizedEmail)
                || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "名前と正しいメールアドレスを入力してください。");
            populateForm(
                    existing,
                    model,
                    name,
                    normalizedEmail,
                    postalCode,
                    prefecture,
                    city,
                    addressLine,
                    building);

            return "mypage-profile";
        }

        if (postalCode == null
                || !postalCode.trim().matches("^[0-9]{3}-?[0-9]{4}$")) {
            model.addAttribute("error", "郵便番号を正しく入力してください。");
            populateForm(
                    existing,
                    model,
                    name,
                    normalizedEmail,
                    postalCode,
                    prefecture,
                    city,
                    addressLine,
                    building);

            return "mypage-profile";
        }

        if (blank(prefecture) || blank(city) || blank(addressLine)) {
            model.addAttribute("error", "都道府県、市区町村、住所を入力してください。");
            populateForm(
                    existing,
                    model,
                    name,
                    normalizedEmail,
                    postalCode,
                    prefecture,
                    city,
                    addressLine,
                    building);

            return "mypage-profile";
        }

        Integer userId = existing.getId();

        if (userService.emailInUseByOtherUser(normalizedEmail, userId)) {
            model.addAttribute("error", "このメールアドレスは既に使用されています。");
            populateForm(
                    existing,
                    model,
                    name,
                    normalizedEmail,
                    postalCode,
                    prefecture,
                    city,
                    addressLine,
                    building);

            return "mypage-profile";
        }

        // フォームから受け取った変更可能な項目だけを既存エンティティへ反映する。
        // ID、パスワード、会員登録日時はフォーム値で上書きしない。
        existing.setName(name.trim());
        existing.setEmail(normalizedEmail);
        existing.setPostalCode(postalCode.trim().replace("-", ""));
        existing.setPrefecture(prefecture.trim());
        existing.setCity(city.trim());
        existing.setAddressLine(addressLine.trim());
        existing.setBuilding(building == null ? "" : building.trim());

        userService.updateProfile(existing);
        redirectAttributes.addFlashAttribute("success", "ユーザー情報を更新しました。");

        return "redirect:/mypage";
    }

    @GetMapping("/mypage/orders")
    public String orderHistory(HttpSession session, Model model) {
        Users user = getLoginUser(session);

        if (user == null) {
            return "redirect:/login";
        }

        List<OrderHistoryView> history = ordersRepository
                .findByUserIdOrderByOrderedAtDesc(user.getId())
                .stream()
                .map(order -> {
                    List<OrderHistoryItem> items = orderDetailsRepository
                            .findByOrderId(order.getId())
                            .stream()
                            .map(detail -> {
                                boolean discontinued = productsRepository
                                        .findById(detail.getProductId())
                                        .map(product ->
                                                Integer.valueOf(9999)
                                                        .equals(product.getCategoryId()))
                                        .orElse(false);

                                return new OrderHistoryItem(
                                        detail.getProductId(),
                                        detail.getProductName(),
                                        detail.getPrice(),
                                        detail.getQuantity(),
                                        discontinued);
                            })
                            .collect(Collectors.toList());

                    return new OrderHistoryView(
                            order.getId(),
                            order.getOrderedAt(),
                            order.getTotalPrice(),
                            order.getStatus(),
                            items);
                })
                .collect(Collectors.toList());

        model.addAttribute("orders", history);

        return "mypage-orders";
    }

    private Users getLoginUser(HttpSession session) {
        Object value = session.getAttribute("userId");

        if (!(value instanceof Integer)) {
            return null;
        }

        return userService.findById((Integer) value);
    }

    private void populateForm(
            Users existing,
            Model model,
            String name,
            String email,
            String postalCode,
            String prefecture,
            String city,
            String addressLine,
            String building) {

        // 入力エラー時も、ユーザーが入力した値をフォームに残す
        Users form = new Users();
        form.setId(existing.getId());
        form.setCreatedAt(existing.getCreatedAt());
        form.setName(name);
        form.setEmail(email);
        form.setPostalCode(postalCode);
        form.setPrefecture(prefecture);
        form.setCity(city);
        form.setAddressLine(addressLine);
        form.setBuilding(building);

        model.addAttribute("user", form);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static class OrderHistoryView {

        private final Integer id;
        private final java.time.LocalDateTime orderedAt;
        private final Integer totalPrice;
        private final String status;
        private final List<OrderHistoryItem> items;

        public OrderHistoryView(
                Integer id,
                java.time.LocalDateTime orderedAt,
                Integer totalPrice,
                String status,
                List<OrderHistoryItem> items) {
            this.id = id;
            this.orderedAt = orderedAt;
            this.totalPrice = totalPrice;
            this.status = status;
            this.items = items;
        }

        public Integer getId() {
            return id;
        }

        public java.time.LocalDateTime getOrderedAt() {
            return orderedAt;
        }

        public Integer getTotalPrice() {
            return totalPrice;
        }

        public String getStatus() {
            return status;
        }

        public List<OrderHistoryItem> getItems() {
            return items;
        }
    }

    public static class OrderHistoryItem {

        private final Integer productId;
        private final String productName;
        private final Integer price;
        private final Integer quantity;
        private final boolean discontinued;

        public OrderHistoryItem(
                Integer productId,
                String productName,
                Integer price,
                Integer quantity,
                boolean discontinued) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.discontinued = discontinued;
        }

        public Integer getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getPrice() {
            return price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public boolean isDiscontinued() {
            return discontinued;
        }

        public Integer getSubtotal() {
            return (price == null ? 0 : price)
                    * (quantity == null ? 0 : quantity);
        }
    }
}