package com.HassanProject.shopapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Order ID: " + order.getOrderId());
        holder.tvUserEmail.setText("User: " + order.getUserEmail());
        holder.tvOrderTotal.setText(String.format("Total: $%.2f", order.getTotalPrice()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        String date = sdf.format(new Date(order.getTimestamp()));
        
        holder.tvOrderStatus.setText("Status: " + order.getStatus() + " | " + date);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserEmail, tvOrderTotal, tvOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}
