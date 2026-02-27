package com.HassanProject.shopapp.models;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String userEmail;
    private List<CartItem> items;
    private double totalPrice;
    private long timestamp;
    private String status; // "PENDING", "SHIPPED", "DELIVERED"

    public Order() {}

    public Order(String orderId, String userId, String userEmail, List<CartItem> items, double totalPrice, long timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
