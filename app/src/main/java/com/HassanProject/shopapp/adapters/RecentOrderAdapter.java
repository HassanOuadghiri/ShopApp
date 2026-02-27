package com.HassanProject.shopapp.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.ViewHolder> {

    private final List<Order> orders;
    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public RecentOrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID (truncated)
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(shortId.length() - 8);
        }
        holder.tvOrderId.setText("Order #" + shortId);

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvOrderDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Items count
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvOrderItems.setText(itemCount + (itemCount == 1 ? " item" : " items"));

        // Total
        holder.tvOrderTotal.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));

        // Status badge with color
        String status = order.getStatus() != null ? order.getStatus() : "PENDING";
        holder.tvOrderStatus.setText(status);

        int badgeColor;
        switch (status.toUpperCase()) {
            case "SHIPPED":
                badgeColor = 0xFF2196F3; // Blue
                break;
            case "DELIVERED":
                badgeColor = 0xFF4CAF50; // Green
                break;
            case "CANCELLED":
                badgeColor = 0xFFF44336; // Red
                break;
            default: // PENDING
                badgeColor = 0xFFFF9800; // Orange
                break;
        }

        if (holder.tvOrderStatus.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) holder.tvOrderStatus.getBackground()).setColor(badgeColor);
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(10 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            bg.setColor(badgeColor);
            holder.tvOrderStatus.setBackground(bg);
        }

        // Show Cancel button only for PENDING orders
        if ("PENDING".equalsIgnoreCase(status)) {
            holder.btnCancelOrder.setVisibility(View.VISIBLE);
            holder.btnCancelOrder.setOnClickListener(v -> cancelOrder(order, holder.getAdapterPosition()));
        } else {
            holder.btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void cancelOrder(Order order, int position) {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        String orderId = order.getOrderId();

        // Update status in user's orders
        mDatabase.child("users").child(uid).child("orders").child(orderId).child("status").setValue("CANCELLED");
        // Also update in global orders (for admin view)
        mDatabase.child("orders").child(orderId).child("status").setValue("CANCELLED");

        // Update local list
        order.setStatus("CANCELLED");
        notifyItemChanged(position);

        Toast.makeText(mAuth.getInstance().getApp().getApplicationContext(), "Order cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderItems, tvOrderTotal;
        Button btnCancelOrder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }
}
