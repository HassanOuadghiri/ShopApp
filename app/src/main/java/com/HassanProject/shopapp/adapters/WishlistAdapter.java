package com.HassanProject.shopapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.models.Product;
import com.HassanProject.shopapp.utils.ProductImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<Product> wishlistProducts;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public WishlistAdapter(List<Product> wishlistProducts) {
        this.wishlistProducts = wishlistProducts;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = wishlistProducts.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("$%.2f", product.getPrice()));

        // Load product image from drawable resource by name
        ProductImageHelper.loadProductImage(holder.ivProductImage, product.getImageUrl(), product.getName(), R.mipmap.ic_launcher);

        holder.btnAddToCart.setOnClickListener(v -> moveFromWishlistToCart(product));

        // Configure remove button
        holder.btnAddToWishlist.setIconResource(0);
        holder.btnAddToWishlist.setText("Remove");
        holder.btnAddToWishlist.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                removeFromWishlist(product, pos);
            }
        });
    }

    private void moveFromWishlistToCart(Product product) {
         // Add to cart logic here (similar to ProductAdapter)
         // ... simplified for brevity, ideally reuse logic or delegate
         Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Moved to Cart (Logic needed)", Toast.LENGTH_SHORT).show();
    }

    private void removeFromWishlist(Product product, int position) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("wishlist").child(product.getId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public int getItemCount() {
        return wishlistProducts.size();
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice;
        ImageView ivProductImage;
        MaterialButton btnAddToCart, btnAddToWishlist;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnAddToWishlist = itemView.findViewById(R.id.btnAddToWishlist);
        }
    }
}
