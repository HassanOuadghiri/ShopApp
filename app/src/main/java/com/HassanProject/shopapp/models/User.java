package com.HassanProject.shopapp.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String email;
    private boolean isAdmin;
    // Map of Cart Item ID to CartItem
    private Map<String, CartItem> cart = new HashMap<>(); 
    // Map of Product ID to boolean (true if in wishlist)
    private Map<String, Boolean> wishlist = new HashMap<>();

    public User() {}

    public User(String uid, String email, boolean isAdmin) {
        this.uid = uid;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public Map<String, CartItem> getCart() { return cart; }
    public void setCart(Map<String, CartItem> cart) { this.cart = cart; }

    public Map<String, Boolean> getWishlist() { return wishlist; }
    public void setWishlist(Map<String, Boolean> wishlist) { this.wishlist = wishlist; }
}
