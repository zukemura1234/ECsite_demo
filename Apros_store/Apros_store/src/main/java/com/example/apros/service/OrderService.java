package com.example.apros.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apros.entity.OrderDetails;
import com.example.apros.entity.Orders;
import com.example.apros.entity.Products;
import com.example.apros.repository.OrderDetailsRepository;
import com.example.apros.repository.OrdersRepository;
import com.example.apros.repository.ProductsRepository;

@Service
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductsRepository productsRepository;

    // エラーの種類
    private static final String STOCK_SHORTAGE = "STOCK_SHORTAGE";

    // 在庫不足・販売終了時のメッセージ
    private static final String STOCK_SHORTAGE_MESSAGE =
            "商品の在庫または販売状況が変更されました。"
            + "カートを確認してください。";

    // コンストラクタ
    public OrderService(
            OrdersRepository ordersRepository,
            OrderDetailsRepository orderDetailsRepository,
            ProductsRepository productsRepository) {

        this.ordersRepository = ordersRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productsRepository = productsRepository;
    }

    // 注文確定処理
    @Transactional
    public Integer placeOrder(
            Integer userId,
            Map<Integer, Integer> cart,
            String name,
            String postalCode,
            String prefecture,
            String city,
            String addressLine,
            String building,
            String paymentMethod) {

        // ログイン・カートの確認
        if (userId == null || cart == null || cart.isEmpty()) {
            throw new IllegalStateException(
                    "注文する商品がありません。");
        }

        // お届け先の確認
        if (blank(name)
                || blank(prefecture)
                || blank(city)
                || blank(addressLine)
                || postalCode == null
                || !postalCode.matches("^[0-9]{3}-?[0-9]{4}$")) {

            throw new IllegalStateException(
                    "お届け先の情報が正しくありません。");
        }

        // お支払い方法の確認
        if (!"代引き".equals(paymentMethod)
                && !"銀行振込".equals(paymentMethod)
                && !"クレジットカード".equals(paymentMethod)) {

            throw new IllegalStateException(
                    "お支払い方法が正しくありません。");
        }

        // DBのカラム長を確認
        if (name.length() > 100
                || prefecture.length() > 20
                || city.length() > 100
                || addressLine.length() > 255
                || (building != null && building.length() > 255)) {

            throw new IllegalStateException(
                    "お届け先の入力文字数が上限を超えています。");
        }

        // 商品ID順にロックを取得
        List<Integer> productIds =
                new ArrayList<>(cart.keySet());

        productIds.sort(Comparator.naturalOrder());

        List<Products> products = new ArrayList<>();
        long totalPrice = 0;

        // 商品の確認・合計金額の計算
        for (Integer productId : productIds) {

            Integer quantity = cart.get(productId);

            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException(
                        "商品の数量が正しくありません。");
            }

            Products product =
                    productsRepository.findByIdForUpdate(productId)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "商品が見つかりません。ID: "
                                                    + productId));

            // 販売終了商品の確認
            if (Integer.valueOf(9999)
                    .equals(product.getCategoryId())) {

                throw new IllegalStateException(STOCK_SHORTAGE);
            }

            // 在庫の確認
            if (product.getStock() == null
                    || product.getStock() < quantity) {

                throw new IllegalStateException(STOCK_SHORTAGE);
            }

            // 価格の確認
            if (product.getPrice() == null
                    || product.getPrice() < 0) {

                throw new IllegalStateException(
                        "商品の価格が正しくありません: "
                                + product.getName());
            }

            // 合計金額の計算
            totalPrice += (long) product.getPrice() * quantity;

            if (totalPrice > Integer.MAX_VALUE) {
                throw new IllegalStateException(
                        "合計金額が上限を超えています。");
            }

            products.add(product);
        }

        // 注文情報の作成
        Orders order = new Orders();

        order.setUserId(userId);
        order.setTotalPrice((int) totalPrice);
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus("注文確定");

        // お届け先情報
        order.setName(name);
        order.setPostalCode(postalCode.replace("-", ""));
        order.setPrefecture(prefecture);
        order.setCity(city);
        order.setAddressLine(addressLine);
        order.setBuilding(building);

        // お支払い方法
        order.setPayment(paymentMethod);

        // ordersテーブルに保存
        order = ordersRepository.save(order);

        // 注文明細の保存・在庫更新
        for (Products product : products) {

            int quantity = cart.get(product.getId());

            // 注文明細の作成
            OrderDetails detail = new OrderDetails();

            detail.setOrderId(order.getId());
            detail.setProductId(product.getId());
            detail.setProductName(product.getName());
            detail.setPrice(product.getPrice());
            detail.setQuantity(quantity);

            // order_detailsテーブルに保存
            orderDetailsRepository.save(detail);

            // 在庫の減算
            product.setStock(product.getStock() - quantity);

            // productsテーブルを更新
            productsRepository.save(product);
        }

        // 登録した注文IDを返す
        return order.getId();
    }

    // 注文情報の取得
    @Transactional(readOnly = true)
    public Orders findOrder(Integer orderId, Integer userId) {

        if (orderId == null || userId == null) {
            return null;
        }

        return ordersRepository.findById(orderId)
                .filter(order ->
                        userId.equals(order.getUserId()))
                .orElse(null);
    }

    // 在庫不足・販売終了の判定
    public boolean isStockShortage(IllegalStateException e) {
        return STOCK_SHORTAGE.equals(e.getMessage());
    }

    // エラーメッセージの取得
    public String getErrorMessage(IllegalStateException e) {

        if (isStockShortage(e)) {
            return STOCK_SHORTAGE_MESSAGE;
        }

        return e.getMessage();
    }

    // 空文字の確認
    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
