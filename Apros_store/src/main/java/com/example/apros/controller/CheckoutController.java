package com.example.apros.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.apros.entity.Orders;
import com.example.apros.entity.Users;
import com.example.apros.service.CartService;
import com.example.apros.service.CheckoutService;
import com.example.apros.service.OrderService;
import com.example.apros.service.UserService;

@Controller
public class CheckoutController {

    private final UserService userService;
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public CheckoutController(UserService userService, CartService cartService,
            CheckoutService checkoutService, OrderService orderService) {
        this.userService = userService;
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    // 配送先・支払方法の入力画面
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Users user = currentUser(session);
        if (user == null) return "redirect:/login?next=checkout";// ユーザーが存在しない場合ログイン画面に遷移
        if (cartService.isEmpty(session)) return "redirect:/cart";// カートの中身が存在しない場合カート画面に遷移

        model.addAttribute("registeredUser", user);

        if (session.getAttribute("checkoutPaymentMethod") != null) { //セッションに以前の支払方法が残っている場合
            Users draft = createAddressUser(
                    (String) session.getAttribute("checkoutName"),
                    (String) session.getAttribute("checkoutPostalCode"),
                    (String) session.getAttribute("checkoutPrefecture"),
                    (String) session.getAttribute("checkoutCity"),
                    (String) session.getAttribute("checkoutAddressLine"),
                    (String) session.getAttribute("checkoutBuilding"));

            String type = (String) session.getAttribute("checkoutShippingType");
            model.addAttribute("user", "registered".equals(type) ? user : draft);
            model.addAttribute("shippingType", type);
            model.addAttribute("paymentMethod", session.getAttribute("checkoutPaymentMethod"));
        } else {
            model.addAttribute("user", user);
        }

        return "checkout";
    }

    // カートの数量変更
    @PostMapping("/checkout/quantity")
    public String changeQuantity(@RequestParam("productId") Integer productId,
            @RequestParam("change") int change, HttpSession session) {

        if (currentUser(session) == null) return "redirect:/login?next=checkout";

        cartService.changeQuantity(productId, change, session);
        return "redirect:/checkout";
    }

    // 配送先・支払方法を確認
    @PostMapping("/checkout/confirm")
    public String confirm(
            @RequestParam("shippingType") String shippingType,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "postalCode", required = false) String postalCode,
            @RequestParam(value = "prefecture", required = false) String prefecture,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "addressLine", required = false) String addressLine,
            @RequestParam(value = "building", required = false) String building,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            HttpSession session, Model model) {

        Users user = currentUser(session);
        if (user == null) return "redirect:/login?next=checkout";
        if (cartService.isEmpty(session)) return "redirect:/cart";

        boolean saved = checkoutService.saveDetails(shippingType, name, postalCode,
                prefecture, city, addressLine, building, paymentMethod, user, session);

        if (!saved) {
            Users input = createAddressUser(name, postalCode, prefecture,
                    city, addressLine, building);

            model.addAttribute("user", "registered".equals(shippingType) ? user : input);
            model.addAttribute("registeredUser", user);
            model.addAttribute("shippingType", shippingType);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("error", "配送先または支払方法を正しく入力してください");
            return "checkout";
        }

        session.setAttribute("checkoutShippingType", shippingType);
        session.setAttribute("checkoutToken", UUID.randomUUID().toString());

        return "redirect:/checkout/confirm";
    }

    // 注文確認画面
    @GetMapping("/checkout/confirm")
    public String showConfirm(HttpSession session, Model model) {
    	//不正アクセス用
        if (currentUser(session) == null) return "redirect:/login?next=checkout";
        if (cartService.isEmpty(session)) return "redirect:/cart";
        if (session.getAttribute("checkoutPaymentMethod") == null
                || session.getAttribute("checkoutToken") == null) {
            return "redirect:/checkout";
        }

        model.addAttribute("name", session.getAttribute("checkoutName"));
        model.addAttribute("postalCode", session.getAttribute("checkoutPostalCode"));
        model.addAttribute("prefecture", session.getAttribute("checkoutPrefecture"));
        model.addAttribute("city", session.getAttribute("checkoutCity"));
        model.addAttribute("addressLine", session.getAttribute("checkoutAddressLine"));
        model.addAttribute("building", session.getAttribute("checkoutBuilding"));
        model.addAttribute("paymentMethod", session.getAttribute("checkoutPaymentMethod"));
        model.addAttribute("orderToken", session.getAttribute("checkoutToken"));

        return "checkout-confirm";
    }

    // 注文確定・DB保存
    @PostMapping("/checkout/place-order")
    @SuppressWarnings("unchecked")
    public String placeOrder(@RequestParam("orderToken") String token,
            HttpSession session, RedirectAttributes redirect) {

        synchronized (session) {
            Users user = currentUser(session);
            if (user == null) return "redirect:/login?next=checkout";

            Integer lastId = (Integer) session.getAttribute("lastOrderId");
            
            // 二重注文防止
            if (token.equals(session.getAttribute("lastOrderToken"))//tokenに被りがない
                    && lastId != null//lastIdがある
                    && orderService.findOrder(lastId, user.getId()) != null) {//lastIdのidでオーダー情報にデータがある
                return "redirect:/checkout/complete";
            }

            if (!token.equals(session.getAttribute("checkoutToken"))
                    || session.getAttribute("checkoutPaymentMethod") == null) {
                return "redirect:/checkout";
            }

            Map<Integer, Integer> cart =
                    (Map<Integer, Integer>) session.getAttribute("cart");

            try {
                Integer orderId = orderService.placeOrder(
                        user.getId(),
                        cart == null ? null : new LinkedHashMap<>(cart),
                        (String) session.getAttribute("checkoutName"),
                        (String) session.getAttribute("checkoutPostalCode"),
                        (String) session.getAttribute("checkoutPrefecture"),
                        (String) session.getAttribute("checkoutCity"),
                        (String) session.getAttribute("checkoutAddressLine"),
                        (String) session.getAttribute("checkoutBuilding"),
                        (String) session.getAttribute("checkoutPaymentMethod"));

                session.setAttribute("lastOrderId", orderId);
                session.setAttribute("lastOrderToken", token);
                session.removeAttribute("cart");
                clearCheckoutSession(session);

                return "redirect:/checkout/complete";

            } catch (IllegalStateException ex) {
                redirect.addFlashAttribute("orderError",
                        orderService.getErrorMessage(ex));
                return "redirect:/checkout/confirm";
            }
        }
    }

    // 注文完了画面
    @GetMapping("/checkout/complete")
    public String complete(HttpSession session, Model model) {
        Users user = currentUser(session);
        if (user == null) return "redirect:/login?next=checkout";

        Integer orderId = (Integer) session.getAttribute("lastOrderId");
        if (orderId == null) return "redirect:/";

        Orders order = orderService.findOrder(orderId, user.getId());
        if (order == null) return "redirect:/";

        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("id", order.getId());
        receipt.put("totalPrice", order.getTotalPrice());
        receipt.put("orderedAt", order.getOrderedAt());
        receipt.put("status", order.getStatus());

        model.addAttribute("order", receipt);
        return "checkout-complete";
    }

    // ログイン中の会員情報を取得
    private Users currentUser(HttpSession session) {
        return userService.findById((Integer) session.getAttribute("userId"));
    }

    // 配送先情報をUsersオブジェクトにまとめる
    private Users createAddressUser(String name, String postalCode,
            String prefecture, String city, String addressLine, String building) {

        Users user = new Users();
        user.setName(name);
        user.setPostalCode(postalCode);
        user.setPrefecture(prefecture);
        user.setCity(city);
        user.setAddressLine(addressLine);
        user.setBuilding(building);
        return user;
    }

    // 注文確定後のセッション情報を削除
    private void clearCheckoutSession(HttpSession session) {
        String[] keys = {"Name", "PostalCode", "Prefecture", "City",
                "AddressLine", "Building", "PaymentMethod", "ShippingType", "Token"};

        for (String key : keys) {
            session.removeAttribute("checkout" + key);
        }
    }
}
