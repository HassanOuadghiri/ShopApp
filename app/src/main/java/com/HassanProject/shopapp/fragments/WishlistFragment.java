package com.HassanProject.shopapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.R;
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

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private List<Product> wishlistProducts;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        wishlistProducts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewWishlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WishlistAdapter(wishlistProducts);
        recyclerView.setAdapter(adapter);

        loadWishlist();
        return view;
    }

    private void loadWishlist() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).child("wishlist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistProducts.clear();
                adapter.notifyDataSetChanged();

                long totalItems = snapshot.getChildrenCount();
                if (totalItems == 0) return;

                final List<Product> tempList = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String productId = itemSnapshot.getKey();
                    fetchProductDetails(productId, tempList, (int) totalItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchProductDetails(String productId, List<Product> tempList, int expectedCount) {
        mDatabase.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    if (product.getId() == null || product.getId().isEmpty()) {
                        product.setId(snapshot.getKey());
                    }
                    tempList.add(product);
                }
                // Once all fetches complete, update the list
                if (tempList.size() >= expectedCount) {
                    wishlistProducts.clear();
                    wishlistProducts.addAll(tempList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
