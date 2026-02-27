package com.HassanProject.shopapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that resolves product names to image files stored in
 * the {@code assets/products/} folder and loads them into ImageViews.
 */
public class ProductImageHelper {

    private static final String TAG = "ProductImageHelper";
    private static final String PRODUCTS_ASSET_DIR = "products";

    private static final Map<String, String> NAME_TO_FILE = new HashMap<>();

    static {
        NAME_TO_FILE.put("playstation 5 console", "ps5.jpg");
        NAME_TO_FILE.put("playstation 5 digital edition", "ps5_digital.jpg");
        NAME_TO_FILE.put("xbox series x", "xbox_seriesx.jpg");
        NAME_TO_FILE.put("nintendo switch oled", "nintendo_switch.jpg");
        NAME_TO_FILE.put("gaming pc rtx 4090 build", "gamingpc_rtx4090.jpg");
        NAME_TO_FILE.put("gaming pc rtx 4070 build", "pc_rtx4070.jpg");
        NAME_TO_FILE.put("gaming pc budget build", "budget_build.jpg");
        NAME_TO_FILE.put("nvidia rtx 4090 24gb", "rtx4090.jpg");
        NAME_TO_FILE.put("nvidia rtx 4070 ti super", "rtx4070.jpg");
        NAME_TO_FILE.put("amd rx 7900 xtx 24gb", "rx7900.jpg");
        NAME_TO_FILE.put("nvidia rtx 4060 8gb", "rtx4060.jpg");
        NAME_TO_FILE.put("amd rx 7600 8gb", "rx7600.jpg");
        NAME_TO_FILE.put("corsair vengeance ddr5 32gb", "vengeance.jpg");
        NAME_TO_FILE.put("g.skill trident z5 rgb 32gb", "gskill.jpg");
        NAME_TO_FILE.put("kingston fury beast ddr4 16gb", "kingston.jpg");
        NAME_TO_FILE.put("corsair dominator platinum 64gb", "corsair.jpg");
        NAME_TO_FILE.put("ps5 dualsense controller", "controller.jpg");
        NAME_TO_FILE.put("gaming monitor 27in 165hz", "monitor.jpg");
        NAME_TO_FILE.put("gaming monitor 27\" 165hz", "monitor.jpg");
        NAME_TO_FILE.put("mechanical keyboard rgb", "keyboard.jpg");
        NAME_TO_FILE.put("gaming headset 7.1 surround", "headset.jpg");
        NAME_TO_FILE.put("gaming mouse 25k dpi", "mouse.jpg");
        NAME_TO_FILE.put("1tb nvme ssd gen4", "nvme.jpg");
    }

    /**
     * Resolves the asset filename for a product image.
     * First tries imageUrl (appending .jpg if needed), then falls back to a name-based lookup.
     */
    public static String resolveAssetFileName(String imageUrl, String productName) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl.endsWith(".jpg") ? imageUrl : imageUrl + ".jpg";
        }
        if (productName != null) {
            String key = productName.toLowerCase().trim();
            String fileName = NAME_TO_FILE.get(key);
            if (fileName != null) {
                return fileName;
            }
        }
        return null;
    }

    /**
     * Loads a product image from {@code assets/products/} into an ImageView.
     * Falls back to the provided fallback resource if no image is found.
     */
    public static void loadProductImage(ImageView imageView, String imageUrl, String productName, int fallbackResId) {
        String fileName = resolveAssetFileName(imageUrl, productName);
        if (fileName != null) {
            Context context = imageView.getContext();
            try {
                InputStream is = context.getAssets().open(PRODUCTS_ASSET_DIR + "/" + fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setBackground(null);
                    return;
                }
            } catch (IOException e) {
                Log.w(TAG, "Product image not found in assets: " + fileName, e);
            }
        }
        imageView.setImageResource(fallbackResId);
    }
}
