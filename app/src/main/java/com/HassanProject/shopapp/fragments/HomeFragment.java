package com.HassanProject.shopapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.adapters.ProductAdapter;
import com.HassanProject.shopapp.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference mDatabase;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        productList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);

        loadProducts();
        return view;
    }

    private void loadProducts() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable timeout = () -> {
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                if (productList.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "Could not load products. Check Firebase & internet.", Toast.LENGTH_LONG).show();
                }
            }
        };
        handler.postDelayed(timeout, 8000);

        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handler.removeCallbacks(timeout);
                productList.clear();
                Log.d(TAG, "Products count: " + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    if (product != null) {
                        if (product.getId() == null || product.getId().isEmpty()) {
                            product.setId(postSnapshot.getKey());
                        }
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (productList.isEmpty() && getActivity() != null) {
                    seedSampleProducts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handler.removeCallbacks(timeout);
                Log.e(TAG, "Firebase error: " + error.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void seedSampleProducts() {
        String[][] products = {
            {"PlayStation 5 Console", "499.99", "Next-gen gaming console with 825GB SSD, 4K 120fps, ray tracing, and DualSense controller.", "ps5"},
            {"PlayStation 5 Digital Edition", "399.99", "All-digital PS5 console. Ultra-fast SSD, 4K gaming, haptic feedback.", "ps5_digital"},
            {"Xbox Series X", "499.99", "Most powerful Xbox ever. 12 TFLOPS, 1TB SSD, 4K at 120fps.", "xbox_seriesx"},
            {"Nintendo Switch OLED", "349.99", "Vibrant 7-inch OLED screen, 64GB storage, enhanced audio.", "nintendo_switch"},
            {"Gaming PC RTX 4090 Build", "2999.99", "Intel i9-14900K, RTX 4090 24GB, 64GB DDR5, 2TB NVMe SSD.", "gamingpc_rtx4090"},
            {"Gaming PC RTX 4070 Build", "1499.99", "AMD Ryzen 7 7800X3D, RTX 4070 12GB, 32GB DDR5, 1TB SSD.", "pc_rtx4070"},
            {"Gaming PC Budget Build", "799.99", "Ryzen 5 5600, RTX 4060 8GB, 16GB DDR4, 512GB NVMe SSD.", "budget_build"},
            {"NVIDIA RTX 4090 24GB", "1599.99", "Flagship GPU, Ada Lovelace, DLSS 3, 24GB GDDR6X.", "rtx4090"},
            {"NVIDIA RTX 4070 Ti Super", "799.99", "16GB GDDR6X, DLSS 3, great for 1440p/4K gaming.", "rtx4070"},
            {"NVIDIA RTX 4060 8GB", "299.99", "Best value 1080p GPU. DLSS 3, low power consumption.", "rtx4060"},
            {"AMD RX 7900 XTX 24GB", "949.99", "24GB GDDR6, RDNA 3, excellent rasterization.", "rx7900"},
            {"AMD RX 7600 8GB", "269.99", "Budget-friendly 1080p GPU with RDNA 3.", "rx7600"},
            {"Corsair Vengeance DDR5 32GB", "109.99", "DDR5-6000 CL30 dual-channel kit (2x16GB).", "vengeance"},
            {"G.Skill Trident Z5 RGB 32GB", "129.99", "DDR5-6400 CL32 with RGB lighting. 2x16GB.", "gskill"},
            {"Kingston Fury Beast DDR4 16GB", "39.99", "DDR4-3200 CL16 (2x8GB). Budget gaming.", "kingston"},
            {"Corsair Dominator Platinum 64GB", "219.99", "DDR5-5600 CL36 (2x32GB). Premium RGB.", "corsair"},
            {"PS5 DualSense Controller", "69.99", "Haptic feedback, adaptive triggers, built-in mic.", "controller"},
            {"Gaming Monitor 27in 165Hz", "299.99", "27-inch QHD IPS, 165Hz, 1ms, G-Sync.", "monitor"},
            {"Mechanical Keyboard RGB", "79.99", "Hot-swappable, per-key RGB, aluminum frame.", "keyboard"},
            {"Gaming Headset 7.1 Surround", "59.99", "7.1 surround, noise-cancelling mic, memory foam.", "headset"},
            {"Gaming Mouse 25K DPI", "49.99", "Lightweight, 25600 DPI, 6 programmable buttons.", "mouse"},
            {"1TB NVMe SSD Gen4", "79.99", "PCIe Gen4, 7000MB/s read. For PS5 and PC.", "nvme"},
        };

        DatabaseReference productsRef = mDatabase.child("products");
        for (String[] p : products) {
            String key = productsRef.push().getKey();
            if (key != null) {
                Product product = new Product(key, p[0], Double.parseDouble(p[1]), p[2], p[3]);
                productsRef.child(key).setValue(product);
            }
        }
        if (getContext() != null) Toast.makeText(getContext(), "Products loaded!", Toast.LENGTH_SHORT).show();
    }
}
