package com.example.order;

public record PlaceOrderRequest(
        String customerId,
        String customerEmail,
        String productId,
        int quantity,
        double totalPrice
) {}
