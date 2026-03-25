package com.example.events;

import java.time.Instant;
import java.util.UUID;

public class OrderPlacedEvent {

    private String orderId;
    private String customerId;
    private String customerEmail;
    private String productId;
    private int quantity;
    private double totalPrice;
    private Instant placedAt;

    // Required by Jackson
    public OrderPlacedEvent() {}

    public OrderPlacedEvent(String customerId, String customerEmail,
                            String productId, int quantity, double totalPrice) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.placedAt = Instant.now();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Instant getPlacedAt() { return placedAt; }
    public void setPlacedAt(Instant placedAt) { this.placedAt = placedAt; }

    @Override
    public String toString() {
        return "OrderPlacedEvent{orderId='%s', customerId='%s', productId='%s', quantity=%d, totalPrice=%.2f}"
                .formatted(orderId, customerId, productId, quantity, totalPrice);
    }
}
