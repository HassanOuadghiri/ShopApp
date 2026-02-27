package com.HassanProject.shopapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.models.CartItem;
import com.HassanProject.shopapp.models.Product;
import com.HassanProject.shopapp.utils.ProductImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the new item_product layout which is a MaterialCardView based layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("$%.2f", product.getPrice()));

        // Load product image from drawable resource by name
        ProductImageHelper.loadProductImage(holder.ivProductImage, product.getImageUrl(), product.getName(), R.mipmap.ic_launcher);

        holder.btnAddToCart.setOnClickListener(v -> addToCart(product));
        holder.btnAddToWishlist.setOnClickListener(v -> addToWishlist(product));
    }

    private void addToCart(Product product) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        
        CartItem item = new CartItem(product.getId(), product.getName(), product.getPrice(), 1, product.getImageUrl());
        
        // Use push() to generate unique key if we wanted multiple separate entries, 
        // but typically cart is keyed by product ID to update quantity.
        // Simplified: Overwrite or increment. Here, just set.
        mDatabase.child("users").child(userId).child("cart").child(product.getId()).setValue(item)
            .addOnSuccessListener(aVoid -> Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Added to Cart", Toast.LENGTH_SHORT).show());
    }

    private void addToWishlist(Product product) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        
        mDatabase.child("users").child(userId).child("wishlist").child(product.getId()).setValue(true)
             .addOnSuccessListener(aVoid -> Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice;
        ImageView ivProductImage;
        // Changed to generic View or MaterialButton to match new layout types if needed, 
        // but Button class works for MaterialButton too.
        Button btnAddToCart, btnAddToWishlist; 

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnAddToWishlist = itemView.findViewById(R.id.btnAddToWishlist);
        }
    }
}
