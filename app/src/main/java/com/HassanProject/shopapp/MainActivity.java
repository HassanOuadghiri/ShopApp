package com.HassanProject.shopapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.HassanProject.shopapp.fragments.CartFragment;
import com.HassanProject.shopapp.fragments.HomeFragment;
import com.HassanProject.shopapp.fragments.ProfileFragment;
import com.HassanProject.shopapp.fragments.WishlistFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView tvToolbarTitle;

    private final HomeFragment homeFragment = new HomeFragment();
    private final CartFragment cartFragment = new CartFragment();
    private final WishlistFragment wishlistFragment = new WishlistFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.fragment_container, wishlistFragment, "wishlist").hide(wishlistFragment)
            .add(R.id.fragment_container, cartFragment, "cart").hide(cartFragment)
            .add(R.id.fragment_container, homeFragment, "home")
            .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                switchFragment(homeFragment, "ShopApp");
            } else if (id == R.id.navigation_cart) {
                switchFragment(cartFragment, "My Cart");
            } else if (id == R.id.navigation_wishlist) {
                switchFragment(wishlistFragment, "Wishlist");
            } else if (id == R.id.navigation_profile) {
                switchFragment(profileFragment, "Profile");
            }
            return true;
        });
    }

    private void switchFragment(Fragment target, String title) {
        if (target == activeFragment) return;
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .hide(activeFragment)
            .show(target)
            .commit();
        activeFragment = target;
        if (tvToolbarTitle != null) tvToolbarTitle.setText(title);
    }
}
