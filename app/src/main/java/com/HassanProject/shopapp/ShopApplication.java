package com.HassanProject.shopapp;

import android.app.Application;
import android.util.Log;
import com.google.firebase.database.FirebaseDatabase;

public class ShopApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        Log.d("ShopApp", "Firebase DB URL: " + database.getReference().toString());
    }
}
