package com.HassanProject.shopapp.utils;

import android.content.Context;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class ProductImageHelper {

    private static final Map<String, String> NAME_TO_DRAWABLE = new HashMap<>();

    static {
        NAME_TO_DRAWABLE.put("playstation 5 console", "ps5");
        NAME_TO_DRAWABLE.put("playstation 5 digital edition", "ps5_digital");
        NAME_TO_DRAWABLE.put("xbox series x", "xbox_seriesx");
        NAME_TO_DRAWABLE.put("nintendo switch oled", "nintendo_switch");
        NAME_TO_DRAWABLE.put("gaming pc rtx 4090 build", "gamingpc_rtx4090");
        NAME_TO_DRAWABLE.put("gaming pc rtx 4070 build", "pc_rtx4070");
        NAME_TO_DRAWABLE.put("gaming pc budget build", "budget_build");
        NAME_TO_DRAWABLE.put("nvidia rtx 4090 24gb", "rtx4090");
        NAME_TO_DRAWABLE.put("nvidia rtx 4070 ti super", "rtx4070");
        NAME_TO_DRAWABLE.put("amd rx 7900 xtx 24gb", "rx7900");
        NAME_TO_DRAWABLE.put("nvidia rtx 4060 8gb", "rtx4060");
        NAME_TO_DRAWABLE.put("amd rx 7600 8gb", "rx7600");
        NAME_TO_DRAWABLE.put("corsair vengeance ddr5 32gb", "vengeance");
        NAME_TO_DRAWABLE.put("g.skill trident z5 rgb 32gb", "gskill");
        NAME_TO_DRAWABLE.put("kingston fury beast ddr4 16gb", "kingston");
        NAME_TO_DRAWABLE.put("corsair dominator platinum 64gb", "corsair");
        NAME_TO_DRAWABLE.put("ps5 dualsense controller", "controller");
        NAME_TO_DRAWABLE.put("gaming monitor 27in 165hz", "monitor");
        NAME_TO_DRAWABLE.put("gaming monitor 27\" 165hz", "monitor");
        NAME_TO_DRAWABLE.put("mechanical keyboard rgb", "keyboard");
        NAME_TO_DRAWABLE.put("gaming headset 7.1 surround", "headset");
        NAME_TO_DRAWABLE.put("gaming mouse 25k dpi", "mouse");
        NAME_TO_DRAWABLE.put("1tb nvme ssd gen4", "nvme");
    }

    /**
     * Resolves the drawable resource name for a product.
     * First tries imageUrl, then falls back to a name-based lookup.
     */
    public static String resolveDrawableName(String imageUrl, String productName) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        if (productName != null) {
            String key = productName.toLowerCase().trim();
            String drawable = NAME_TO_DRAWABLE.get(key);
            if (drawable != null) {
                return drawable;
            }
        }
        return null;
    }

    /**
     * Loads the product image into an ImageView.
     * Falls back to the app launcher icon if no image is found.
     */
    public static void loadProductImage(ImageView imageView, String imageUrl, String productName, int fallbackResId) {
        String drawableName = resolveDrawableName(imageUrl, productName);
        if (drawableName != null) {
            Context context = imageView.getContext();
            int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                imageView.setBackground(null);
                return;
            }
        }
        imageView.setImageResource(fallbackResId);
    }
}
