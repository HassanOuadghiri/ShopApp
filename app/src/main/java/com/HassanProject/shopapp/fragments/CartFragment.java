package com.HassanProject.shopapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.adapters.CartAdapter;
import com.HassanProject.shopapp.models.CartItem;
import com.HassanProject.shopapp.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private MaterialButton btnCheckout;
    private double totalAmount = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        cartList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartList);
        recyclerView.setAdapter(adapter);

        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> placeOrder());

        loadCart();
        return view;
    }

    private void loadCart() {
        if (mAuth.getCurrentUser() == null) return;
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
                if (btnCheckout != null) {
                    btnCheckout.setText("Place Order ($" + String.format("%.2f", totalAmount) + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void placeOrder() {
        if (cartList.isEmpty()) {
            if (getContext() != null) Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        // Check if user has a shipping address before allowing order
        mDatabase.child("users").child(uid).child("shippingAddress").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChild("fullName") || !snapshot.hasChild("street") || !snapshot.hasChild("city")) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Please add a shipping address in your Profile first", Toast.LENGTH_LONG).show();
                    }
                    // Navigate to Profile tab (index 3)
                    if (getActivity() instanceof com.HassanProject.shopapp.MainActivity) {
                        com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                        if (nav != null) {
                            nav.setSelectedItemId(R.id.navigation_profile);
                        }
                    }
                    return;
                }
                // Address exists, proceed with order
                submitOrder(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) Toast.makeText(getContext(), "Failed to check address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitOrder(String uid) {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String orderId = mDatabase.child("orders").push().getKey();
        long timestamp = System.currentTimeMillis();

        if (orderId == null) {
            if (getContext() != null) Toast.makeText(getContext(), "Failed to create order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(orderId, uid, userEmail, new ArrayList<>(cartList), totalAmount, timestamp, "PENDING");

        // Save to global orders AND user-specific orders for reliable retrieval
        Map<String, Object> updates = new HashMap<>();
        updates.put("orders/" + orderId, order);
        updates.put("users/" + uid + "/orders/" + orderId, order);

        mDatabase.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                mDatabase.child("users").child(uid).child("cart").removeValue();
                if (getContext() != null) Toast.makeText(getContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) Toast.makeText(getContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
