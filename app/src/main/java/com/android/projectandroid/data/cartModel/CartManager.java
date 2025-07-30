package com.android.projectandroid.data.cartModel;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.projectandroid.data.productModel.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String CART_PREFS = "cart_preferences";
    private static final String KEY_CART_ITEMS = "cart_items";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private static CartManager instance;

    private CartManager(Context context) {
        prefs = context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    // Lấy danh sách sản phẩm trong giỏ hàng
    public List<Product> getCartItems() {
        String cartJson = prefs.getString(KEY_CART_ITEMS, "");
        if (cartJson.isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<Product>>() {}.getType();
        List<Product> cartItems = gson.fromJson(cartJson, listType);
        return cartItems != null ? cartItems : new ArrayList<>();
    }

    // Thêm sản phẩm vào giỏ hàng
    public boolean addToCart(Product product) {
        List<Product> cartItems = getCartItems();

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        for (Product item : cartItems) {
            if (item.getId().equals(product.getId())) {
                return false; // Sản phẩm đã có trong giỏ hàng
            }
        }

        cartItems.add(product);
        saveCartItems(cartItems);
        return true;
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public boolean removeFromCart(String productId) {
        List<Product> cartItems = getCartItems();

        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(productId)) {
                cartItems.remove(i);
                saveCartItems(cartItems);
                return true;
            }
        }
        return false;
    }

    // Kiểm tra sản phẩm có trong giỏ hàng không
    public boolean isInCart(String productId) {
        List<Product> cartItems = getCartItems();

        for (Product item : cartItems) {
            if (item.getId().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    // Lưu danh sách giỏ hàng
    private void saveCartItems(List<Product> cartItems) {
        String cartJson = gson.toJson(cartItems);
        editor.putString(KEY_CART_ITEMS, cartJson);
        editor.apply();
    }

    // Đếm số lượng sản phẩm trong giỏ hàng
    public int getCartItemCount() {
        return getCartItems().size();
    }

    // Xóa toàn bộ giỏ hàng
    public void clearCart() {
        editor.remove(KEY_CART_ITEMS);
        editor.apply();
    }

    // Cập nhật sản phẩm trong giỏ hàng (dùng khi status thay đổi)
    public void updateProductInCart(Product updatedProduct) {
        List<Product> cartItems = getCartItems();

        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId().equals(updatedProduct.getId())) {
                cartItems.set(i, updatedProduct);
                saveCartItems(cartItems);
                return;
            }
        }
    }

    // Xóa sản phẩm sau khi mua thành công
    public boolean removeAfterPurchase(String productId) {
        return removeFromCart(productId);
    }
}