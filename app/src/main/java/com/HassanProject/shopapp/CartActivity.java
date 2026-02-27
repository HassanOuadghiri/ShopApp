package com.HassanProject.shopapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.adapters.CartAdapter;
import com.HassanProject.shopapp.models.CartItem;
import com.HassanProject.shopapp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Button btnCheckout;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        cartList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(cartList);
        recyclerView.setAdapter(adapter);

        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> placeOrder());

        loadCart();
    }

    private void loadCart() {
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                totalAmount = 0.0;
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem item = itemSnapshot.getValue(CartItem.class);
                    if (item != null) {
                        cartList.add(item);
                        totalAmount += (item.getPrice() * item.getQuantity());
                    }
                }
                adapter.notifyDataSetChanged();
                btnCheckout.setText("Place Order (Total: $" + String.format("%.2f", totalAmount) + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void placeOrder() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // Check if user has a shipping address before allowing order
        mDatabase.child("users").child(uid).child("shippingAddress").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChild("fullName") || !snapshot.hasChild("street") || !snapshot.hasChild("city")) {
                    Toast.makeText(CartActivity.this, "Please add a shipping address in your Profile first", Toast.LENGTH_LONG).show();
                    return;
                }
                submitOrder(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to check address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitOrder(String uid) {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String orderId = mDatabase.child("orders").push().getKey();
        long timestamp = System.currentTimeMillis();

        if (orderId == null) {
            Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(orderId, uid, userEmail, new ArrayList<>(cartList), totalAmount, timestamp, "PENDING");

        // Save to global orders AND user-specific orders
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("orders/" + orderId, order);
        updates.put("users/" + uid + "/orders/" + orderId, order);

        mDatabase.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                // Clear Cart
                mDatabase.child("users").child(uid).child("cart").removeValue();
                Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            })
            .addOnFailureListener(e -> Toast.makeText(CartActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show());
    }
}
