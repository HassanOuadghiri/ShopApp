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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText("Qty: " + item.getQuantity());
        holder.tvPrice.setText(String.format("$%.2f", item.getPrice() * item.getQuantity()));

        // Load product image from drawable resource by name
        ProductImageHelper.loadProductImage(holder.ivImage, item.getImageUrl(), item.getProductName(), R.mipmap.ic_launcher);

        holder.itemView.setOnLongClickListener(v -> {
            removeItem(item);
            return true;
        });
    }

    private void removeItem(CartItem item) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).child("cart").child(item.getProductId()).removeValue()
            .addOnSuccessListener(aVoid -> Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Removed from Cart", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;
        ImageView ivImage;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCartItemImage);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
        }
    }
}
