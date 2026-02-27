package com.HassanProject.shopapp.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HassanProject.shopapp.AdminOrdersActivity;
import com.HassanProject.shopapp.LoginActivity;
import com.HassanProject.shopapp.R;
import com.HassanProject.shopapp.adapters.RecentOrderAdapter;
import com.HassanProject.shopapp.models.Order;
import com.HassanProject.shopapp.models.Product;
import com.HassanProject.shopapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tvUserEmail, tvUserRole, tvNoOrders;
    private MaterialButton btnAdminOrders, btnAddDummy, btnLogout, btnSaveAddress;

    // Shipping address fields
    private TextInputEditText etFullName, etStreetAddress, etCity, etPostalCode, etState, etPhone;
    private AutoCompleteTextView actvCountry;

    // Collapsible sections
    private LinearLayout addressFormContainer, ordersContentContainer;
    private LinearLayout addressHeader, ordersHeader;
    private ImageView ivAddressArrow, ivOrdersArrow;
    private TextView tvAddressSummary, tvOrdersSummary, tvOrderCount;
    private boolean isAddressExpanded = false;
    private boolean isOrdersExpanded = false;

    // Recent orders
    private RecyclerView recyclerViewOrders;
    private RecentOrderAdapter orderAdapter;
    private List<Order> recentOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Profile header
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserRole = view.findViewById(R.id.tvUserRole);

        // Shipping address fields
        etFullName = view.findViewById(R.id.etFullName);
        etStreetAddress = view.findViewById(R.id.etStreetAddress);
        etCity = view.findViewById(R.id.etCity);
        etPostalCode = view.findViewById(R.id.etPostalCode);
        etState = view.findViewById(R.id.etState);
        etPhone = view.findViewById(R.id.etPhone);
        actvCountry = view.findViewById(R.id.actvCountry);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);

        // Address collapsible
        addressHeader = view.findViewById(R.id.addressHeader);
        addressFormContainer = view.findViewById(R.id.addressFormContainer);
        ivAddressArrow = view.findViewById(R.id.ivAddressArrow);
        tvAddressSummary = view.findViewById(R.id.tvAddressSummary);

        // Orders collapsible
        ordersHeader = view.findViewById(R.id.ordersHeader);
        ordersContentContainer = view.findViewById(R.id.ordersContentContainer);
        ivOrdersArrow = view.findViewById(R.id.ivOrdersArrow);
        tvOrdersSummary = view.findViewById(R.id.tvOrdersSummary);
        tvOrderCount = view.findViewById(R.id.tvOrderCount);

        // Setup country dropdown
        setupCountryDropdown();

        // Recent orders
        tvNoOrders = view.findViewById(R.id.tvNoOrders);
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        recentOrders = new ArrayList<>();
        orderAdapter = new RecentOrderAdapter(recentOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);

        // Admin & logout buttons
        btnAdminOrders = view.findViewById(R.id.btnAdminOrders);
        btnAddDummy = view.findViewById(R.id.btnAddDummy);
        btnLogout = view.findViewById(R.id.btnLogout);

        if (mAuth.getCurrentUser() != null) {
            tvUserEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        // ===== Collapsible header clicks =====
        addressHeader.setOnClickListener(v -> toggleAddressSection());
        ordersHeader.setOnClickListener(v -> toggleOrdersSection());

        // Save address: save + collapse
        btnSaveAddress.setOnClickListener(v -> saveShippingAddress());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        btnAdminOrders.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminOrdersActivity.class));
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btnAddDummy.setOnClickListener(v -> addDummyProduct());

        checkAdmin();
        loadShippingAddress();
        loadRecentOrders();

        return view;
    }

    // ===== Expand / Collapse Animations =====

    private void toggleAddressSection() {
        isAddressExpanded = !isAddressExpanded;
        if (isAddressExpanded) {
            tvAddressSummary.setVisibility(View.GONE);
            expandView(addressFormContainer);
            rotateArrow(ivAddressArrow, 0f, 180f);
        } else {
            collapseView(addressFormContainer);
            tvAddressSummary.setVisibility(View.VISIBLE);
            rotateArrow(ivAddressArrow, 180f, 0f);
        }
    }

    private void toggleOrdersSection() {
        isOrdersExpanded = !isOrdersExpanded;
        if (isOrdersExpanded) {
            tvOrdersSummary.setVisibility(View.GONE);
            expandView(ordersContentContainer);
            rotateArrow(ivOrdersArrow, 0f, 180f);
        } else {
            collapseView(ordersContentContainer);
            tvOrdersSummary.setVisibility(View.VISIBLE);
            rotateArrow(ivOrdersArrow, 180f, 0f);
        }
    }

    private void expandView(final View v) {
        v.setVisibility(View.VISIBLE);
        v.measure(View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setAlpha(0f);
        ValueAnimator anim = ValueAnimator.ofInt(0, targetHeight);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.setAlpha(animation.getAnimatedFraction());
            v.requestLayout();
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                v.setAlpha(1f);
            }
        });
        anim.start();
    }

    private void collapseView(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(initialHeight, 0);
        anim.setDuration(250);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.setAlpha(1f - animation.getAnimatedFraction());
            v.requestLayout();
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                v.setVisibility(View.GONE);
                v.setAlpha(1f);
            }
        });
        anim.start();
    }

    private void rotateArrow(ImageView arrow, float from, float to) {
        arrow.animate().rotation(to).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
    }

    // ===== Address Summary =====

    private void updateAddressSummary() {
        String name = getEditText(etFullName);
        String street = getEditText(etStreetAddress);
        String city = getEditText(etCity);
        String country = actvCountry.getText() != null ? actvCountry.getText().toString().trim() : "";

        if (name.isEmpty() && street.isEmpty()) {
            tvAddressSummary.setText("No address saved — tap to add");
        } else {
            StringBuilder sb = new StringBuilder();
            if (!name.isEmpty()) sb.append(name);
            if (!street.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(street);
            }
            if (!city.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(city);
            }
            if (!country.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }
            tvAddressSummary.setText(sb.toString());
        }
    }

    // ===== Country Dropdown =====

    private void setupCountryDropdown() {
        if (getContext() == null) return;
        String[] countries = getResources().getStringArray(R.array.countries);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                countries
        );
        actvCountry.setAdapter(countryAdapter);
        actvCountry.setThreshold(1);
    }

    // ===== Shipping Address =====

    private void saveShippingAddress() {
        if (mAuth.getCurrentUser() == null) {
            showToast("Please sign in first");
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        String fullName = getEditText(etFullName);
        String street = getEditText(etStreetAddress);
        String city = getEditText(etCity);
        String postalCode = getEditText(etPostalCode);
        String state = getEditText(etState);
        String country = actvCountry.getText() != null ? actvCountry.getText().toString().trim() : "";
        String phone = getEditText(etPhone);

        if (fullName.isEmpty() || street.isEmpty() || city.isEmpty()) {
            showToast("Please fill in name, street, and city");
            return;
        }

        btnSaveAddress.setEnabled(false);
        btnSaveAddress.setText("Saving...");

        Map<String, Object> address = new HashMap<>();
        address.put("fullName", fullName);
        address.put("street", street);
        address.put("city", city);
        address.put("postalCode", postalCode);
        address.put("state", state);
        address.put("country", country);
        address.put("phone", phone);

        Log.d(TAG, "Saving address for uid: " + uid + " -> " + address.toString());

        mDatabase.child("users").child(uid).child("shippingAddress").setValue(address)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Address saved successfully");
                btnSaveAddress.setEnabled(true);
                btnSaveAddress.setText("Save Address");
                showToast("Address saved!");
                updateAddressSummary();
                // Collapse after saving
                if (isAddressExpanded) {
                    toggleAddressSection();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save address: " + e.getMessage(), e);
                btnSaveAddress.setEnabled(true);
                btnSaveAddress.setText("Save Address");
                showToast("Failed: " + e.getMessage());
            });
    }

    private void loadShippingAddress() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).child("shippingAddress")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No shipping address found");
                        tvAddressSummary.setText("No address saved — tap to add");
                        return;
                    }
                    Log.d(TAG, "Loaded address: " + snapshot.getValue());
                    setField(etFullName, snapshot.child("fullName"));
                    setField(etStreetAddress, snapshot.child("street"));
                    setField(etCity, snapshot.child("city"));
                    setField(etPostalCode, snapshot.child("postalCode"));
                    setField(etState, snapshot.child("state"));
                    setField(etPhone, snapshot.child("phone"));
                    // Set country dropdown
                    if (snapshot.child("country").exists() && snapshot.child("country").getValue() != null) {
                        String country = snapshot.child("country").getValue(String.class);
                        actvCountry.setText(country, false);
                    }
                    updateAddressSummary();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load address: " + error.getMessage());
                }
            });
    }

    private void setField(TextInputEditText field, DataSnapshot snap) {
        if (snap.exists() && snap.getValue() != null) {
            field.setText(snap.getValue(String.class));
        }
    }

    private String getEditText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    // ===== Recent Orders =====

    private void loadRecentOrders() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "Loading orders for uid: " + uid);

        mDatabase.child("users").child(uid).child("orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "User orders snapshot: childrenCount=" + snapshot.getChildrenCount());
                recentOrders.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null) {
                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(orderSnap.getKey());
                        }
                        recentOrders.add(order);
                    }
                }
                // Sort newest first
                Collections.sort(recentOrders, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                // Keep only last 10
                if (recentOrders.size() > 10) {
                    recentOrders.subList(10, recentOrders.size()).clear();
                }

                orderAdapter.notifyDataSetChanged();

                // Update order count badge
                int count = recentOrders.size();
                if (count > 0) {
                    tvOrderCount.setText(String.valueOf(count));
                    tvOrderCount.setVisibility(View.VISIBLE);
                    tvOrdersSummary.setText(count + " order" + (count == 1 ? "" : "s") + " — tap to view");
                } else {
                    tvOrderCount.setVisibility(View.GONE);
                    tvOrdersSummary.setText("No orders yet");
                }

                if (recentOrders.isEmpty()) {
                    tvNoOrders.setVisibility(View.VISIBLE);
                    recyclerViewOrders.setVisibility(View.GONE);
                } else {
                    tvNoOrders.setVisibility(View.GONE);
                    recyclerViewOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders: " + error.getMessage());
            }
        });
    }

    // ===== Admin =====

    private void checkAdmin() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.isAdmin()) {
                    tvUserRole.setText("Administrator");
                    btnAdminOrders.setVisibility(View.VISIBLE);
                    btnAddDummy.setVisibility(View.VISIBLE);
                } else {
                    tvUserRole.setText("Customer");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addDummyProduct() {
        String key = mDatabase.child("products").push().getKey();
        Product p = new Product(key, "Sample Product " + (System.currentTimeMillis() % 1000),
                10.0 + (System.currentTimeMillis() % 100),
                "This is a dummy product for testing.", "");
        if (key != null) {
            mDatabase.child("products").child(key).setValue(p);
            showToast("Product added!");
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
