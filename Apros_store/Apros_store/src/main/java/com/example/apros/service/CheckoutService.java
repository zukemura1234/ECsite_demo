package com.example.apros.service;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.example.apros.entity.Users;

@Service
public class CheckoutService {
    public boolean saveDetails(String shippingType, String name, String postalCode,
            String prefecture, String city, String addressLine, String building,
            String paymentMethod, Users registeredUser, HttpSession session) {

        if ("registered".equals(shippingType)) {
            name = registeredUser.getName();
            postalCode = registeredUser.getPostalCode();
            prefecture = registeredUser.getPrefecture();
            city = registeredUser.getCity();
            addressLine = registeredUser.getAddressLine();
            building = registeredUser.getBuilding();
        } else if (!"new".equals(shippingType)) {
            return false;
        }

        if (blank(name) || postalCode == null || !postalCode.matches("^[0-9]{3}-?[0-9]{4}$")
                || blank(prefecture) || blank(city) || blank(addressLine)) {
            return false;
        }
        if (!"代引き".equals(paymentMethod) && !"銀行振込".equals(paymentMethod)
                && !"クレジットカード".equals(paymentMethod)) {
            return false;
        }

        session.setAttribute("checkoutName", name);
        session.setAttribute("checkoutPostalCode", postalCode.replace("-", ""));
        session.setAttribute("checkoutPrefecture", prefecture);
        session.setAttribute("checkoutCity", city);
        session.setAttribute("checkoutAddressLine", addressLine);
        session.setAttribute("checkoutBuilding", building);
        session.setAttribute("checkoutPaymentMethod", paymentMethod);
        return true;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
