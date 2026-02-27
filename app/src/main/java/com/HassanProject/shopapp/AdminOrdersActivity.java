package com.HassanProject.shopapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.adapters.OrderAdapter;
import com.HassanProject.shopapp.models.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        orderList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        loadAllOrders();
    }

    private void loadAllOrders() {
        mDatabase.child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }
                // Determine sorting? Currently unsorted or by push key (chronological roughly)
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
