package com.HassanProject.shopapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.HassanProject.shopapp.adapters.ProductAdapter;
import com.HassanProject.shopapp.models.Product;
import com.HassanProject.shopapp.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        productList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewProducts);
        // Use GridLayoutManager for a modern e-commerce look (2 columns)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
        
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Already here
                return true;
            } else if (id == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.navigation_wishlist) {
                startActivity(new Intent(this, WishlistActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.navigation_profile) {
                checkAdminMenu();
                return true;
            }
            return false;
        });

        // Assuming default checks
        checkAdmin();
        loadProducts();
    }
    
    private void checkAdminMenu() {
        // For now, redirect to login as a placeholder for profile/admin options
        // Or show a dialogue etc.
        Toast.makeText(this, "Profile / Admin", Toast.LENGTH_SHORT).show();
        // If needed, sign out:
        // mAuth.signOut();
        // startActivity(new Intent(this, LoginActivity.class));
        // finish();
    }

    private void checkAdmin() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.isAdmin()) {
                    Toast.makeText(ProductListActivity.this, "Admin Access Granted", Toast.LENGTH_SHORT).show();
                    invalidateOptionsMenu(); // re-create menu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
         if (mAuth.getCurrentUser() == null) return super.onPrepareOptionsMenu(menu);
         String uid = mAuth.getCurrentUser().getUid();
         mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 User user = snapshot.getValue(User.class);
                 if (user != null && user.isAdmin()) {
                     menu.findItem(R.id.action_admin_orders).setVisible(true);
                     menu.findItem(R.id.action_add_dummy).setVisible(true);
                 } else {
                     menu.findItem(R.id.action_admin_orders).setVisible(false);
                     menu.findItem(R.id.action_add_dummy).setVisible(false);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {}
         });
         return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_admin_orders) {
            startActivity(new Intent(this, AdminOrdersActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_add_dummy) {
            addDummyProduct();
            return true;
        } else if (id == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_wishlist) {
            startActivity(new Intent(this, WishlistActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String TAG = "ProductListActivity";

    private void loadProducts() {
        if(swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        
        // Timeout: stop spinner after 8 seconds if no response
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable timeout = () -> {
            if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                if(productList.isEmpty()) {
                    Toast.makeText(ProductListActivity.this, 
                        "Could not load products. Check Firebase rules & internet.", 
                        Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Timeout: No products loaded. Check Firebase DB rules — set .read to true or \"auth != null\"");
                }
            }
        };
        handler.postDelayed(timeout, 8000);
        
        Log.d(TAG, "Loading products from: " + mDatabase.child("products").toString());
        
        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handler.removeCallbacks(timeout);
                productList.clear();
                Log.d(TAG, "Products snapshot exists: " + snapshot.exists() + ", count: " + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    if(product != null) {
                        if (product.getId() == null || product.getId().isEmpty()) {
                            product.setId(postSnapshot.getKey());
                        }
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                if(swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                
                if(productList.isEmpty()) {
                    Toast.makeText(ProductListActivity.this, "No products found. Seeding...", Toast.LENGTH_SHORT).show();
                    seedSampleProducts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handler.removeCallbacks(timeout);
                Log.e(TAG, "Firebase error: " + error.getMessage() + " | Code: " + error.getCode());
                Toast.makeText(ProductListActivity.this, 
                    "Error: " + error.getMessage() + "\nFix Firebase DB rules!", 
                    Toast.LENGTH_LONG).show();
                if(swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void addDummyProduct() {
        String key = mDatabase.child("products").push().getKey();
        Product p = new Product(key, "Sample Product " + (System.currentTimeMillis() % 1000), 
                                10.0 + (System.currentTimeMillis() % 100), 
                                "This is a dummy product description for testing UI.", 
                                "https://via.placeholder.com/150"); 
        if(key != null) mDatabase.child("products").child(key).setValue(p);
    }

    private void seedProductsIfEmpty() {
        mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    seedSampleProducts();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void seedSampleProducts() {
        String[][] products = {
            // Consoles
            {"PlayStation 5 Console", "499.99", "Next-gen gaming console with 825GB SSD, 4K 120fps, ray tracing, and DualSense controller.", "ps5"},
            {"PlayStation 5 Digital Edition", "399.99", "All-digital PS5 console — no disc drive. Ultra-fast SSD, 4K gaming, and haptic feedback.", "ps5_digital"},
            {"Xbox Series X", "499.99", "Most powerful Xbox ever. 12 TFLOPS, 1TB SSD, 4K at 120fps, backward compatible.", "xbox_seriesx"},
            {"Nintendo Switch OLED", "349.99", "Vibrant 7-inch OLED screen, 64GB storage, enhanced audio, and wide adjustable stand.", "nintendo_switch"},
            // Gaming PCs
            {"Gaming PC RTX 4090 Build", "2999.99", "Ultimate gaming rig: Intel i9-14900K, RTX 4090 24GB, 64GB DDR5, 2TB NVMe SSD.", "gamingpc_rtx4090"},
            {"Gaming PC RTX 4070 Build", "1499.99", "High-performance tower: AMD Ryzen 7 7800X3D, RTX 4070 12GB, 32GB DDR5, 1TB SSD.", "pc_rtx4070"},
            {"Gaming PC Budget Build", "799.99", "Entry-level gaming: Ryzen 5 5600, RTX 4060 8GB, 16GB DDR4, 512GB NVMe SSD.", "budget_build"},
            // GPUs
            {"NVIDIA RTX 4090 24GB", "1599.99", "Flagship GPU with Ada Lovelace architecture, DLSS 3, 24GB GDDR6X, ray tracing.", "rtx4090"},
            {"NVIDIA RTX 4070 Ti Super", "799.99", "Excellent 1440p/4K performance. 16GB GDDR6X, DLSS 3, great for AAA games.", "rtx4070"},
            {"NVIDIA RTX 4060 8GB", "299.99", "Best value 1080p GPU. 8GB GDDR6, DLSS 3, low power consumption.", "rtx4060"},
            {"AMD RX 7900 XTX 24GB", "949.99", "AMD's flagship: 24GB GDDR6, RDNA 3 architecture, excellent rasterization.", "rx7900"},
            {"AMD RX 7600 8GB", "269.99", "Budget-friendly 1080p gaming GPU with RDNA 3 and 8GB GDDR6.", "rx7600"},
            // RAM
            {"Corsair Vengeance DDR5 32GB", "109.99", "DDR5-6000 CL30 dual-channel kit (2x16GB). Optimized for Intel & AMD platforms.", "vengeance"},
            {"G.Skill Trident Z5 RGB 32GB", "129.99", "DDR5-6400 CL32 with stunning RGB lighting. 2x16GB for high-end builds.", "gskill"},
            {"Kingston Fury Beast DDR4 16GB", "39.99", "DDR4-3200 CL16 (2x8GB). Reliable and affordable for budget gaming rigs.", "kingston"},
            {"Corsair Dominator Platinum 64GB", "219.99", "DDR5-5600 CL36 (2x32GB). Premium RGB, ideal for content creators.", "corsair"},
            // Peripherals & Accessories
            {"PS5 DualSense Controller", "69.99", "Wireless controller with haptic feedback, adaptive triggers, and built-in mic.", "controller"},
            {"Gaming Monitor 27\" 165Hz", "299.99", "27-inch QHD IPS, 165Hz, 1ms response, G-Sync compatible. Perfect for competitive play.", "monitor"},
            {"Mechanical Keyboard RGB", "79.99", "Hot-swappable switches, per-key RGB, aluminum frame. Cherry MX compatible.", "keyboard"},
            {"Gaming Headset 7.1 Surround", "59.99", "Virtual 7.1 surround sound, noise-cancelling mic, memory foam ear cushions.", "headset"},
            {"Gaming Mouse 25K DPI", "49.99", "Lightweight esports mouse, 25,600 DPI sensor, 6 programmable buttons.", "mouse"},
            {"1TB NVMe SSD Gen4", "79.99", "PCIe Gen4 NVMe SSD with 7000MB/s read speed. Ideal for PS5 and PC.", "nvme"},
        };

        DatabaseReference productsRef = mDatabase.child("products");
        for (String[] p : products) {
            String key = productsRef.push().getKey();
            if (key != null) {
                Product product = new Product(key, p[0], Double.parseDouble(p[1]), p[2], p[3]);
                productsRef.child(key).setValue(product);
            }
        }
        Toast.makeText(this, "Products loaded!", Toast.LENGTH_SHORT).show();
    }
}
