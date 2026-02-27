package com.HassanProject.shopapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.adapters.WishlistAdapter;
import com.HassanProject.shopapp.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private List<Product> wishlistProducts; // List of full product objects
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        wishlistProducts = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewWishlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WishlistAdapter(wishlistProducts);
        recyclerView.setAdapter(adapter);

        loadWishlist();
    }

    private void loadWishlist() {
        String uid = mAuth.getCurrentUser().getUid();
        // 1. Get wishlist product IDs
        mDatabase.child("users").child(uid).child("wishlist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistProducts.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String productId = itemSnapshot.getKey();
                    // 2. Fetch product details for each ID
                    fetchProductDetails(productId);
                }
                // Note: The adapter notify might happen before all products are loaded due to async nature.
                // Better approach involves counting or using diff util, but for simplicity:
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchProductDetails(String productId) {
        mDatabase.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    wishlistProducts.add(product);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
