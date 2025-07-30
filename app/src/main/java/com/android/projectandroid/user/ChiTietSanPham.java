package com.android.projectandroid.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.projectandroid.R;
import com.android.projectandroid.data.productModel.Product;
import com.android.projectandroid.data.productModel.ProductImageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.List;

public class ChiTietSanPham extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    public static final String EXTRA_PRODUCT = "extra_product";

    // Views
    private ImageButton btnBack;
    private ImageButton btnFavorite;
    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutDots;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductCondition;
    private TextView tvProductCategory;
    private TextView tvProductDescription;
    private TextView tvShortDescription;
    private TextView btnBuyNow;

    // Data
    private Product product;
    private DecimalFormat formatter;
    private boolean isFavorite = false;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.product_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        formatter = new DecimalFormat("#,###");

        // Get current user ID
        getCurrentUserId();

        // Get product from intent
        getProductFromIntent();

        // Initialize views
        initViews();

        // Setup views
        setupViews();

        // Load product data
        loadProductData();
    }

    private void getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");
    }

    private void getProductFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PRODUCT)) {
            product = (Product) intent.getSerializableExtra(EXTRA_PRODUCT);
            Log.d(TAG, "Received product: " + (product != null ? product.getProductName() : "null"));
        }

        if (product == null) {
            Log.e(TAG, "No product data received");
            Toast.makeText(this, "Lỗi: Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        viewPagerImages = findViewById(R.id.viewpager_images);
        tabLayoutDots = findViewById(R.id.tablayout_dots);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductCondition = findViewById(R.id.tv_product_condition);
        tvProductCategory = findViewById(R.id.tv_product_category);
        tvProductDescription = findViewById(R.id.tv_product_description);
        tvShortDescription = findViewById(R.id.short_description);
        btnBuyNow = findViewById(R.id.btn_mua_hang); // ✅ THÊM NÚT MUA HÀNG
    }

    private void setupViews() {
        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            onBackPressed();
        });

        // Favorite button
        btnFavorite.setOnClickListener(v -> {
            Log.d(TAG, "Favorite button clicked");
            toggleFavorite();
        });

        // ✅ BUY NOW BUTTON
        btnBuyNow.setOnClickListener(v -> {
            Log.d(TAG, "Buy now button clicked");
            handleBuyNowClick();
        });
    }

    private void loadProductData() {
        if (product == null) return;

        try {
            // Product name
            tvProductName.setText(product.getProductName());

            // Product price - ưu tiên sellPrice, fallback về purchasePrice
            double displayPrice = product.getSellPrice() > 0 ? product.getSellPrice() : product.getPurchasePrice();
            String formattedPrice = formatter.format(displayPrice) + " VNĐ";
            tvProductPrice.setText(formattedPrice);

            // Short description
            tvShortDescription.setText(product.getShortDescription());

            // Product condition
            String rawCondition = product.getCondition();
            if (rawCondition != null) {
                int idx = rawCondition.indexOf('(');
                String displayCondition = idx != -1 ? rawCondition.substring(0, idx).trim() : rawCondition;
                tvProductCondition.setText(displayCondition);
            }

            // Product category
            tvProductCategory.setText(product.getCategory() != null ? product.getCategory() : "Chưa phân loại");

            // Product description
            tvProductDescription.setText(product.getDetailedDescription() != null ? product.getDetailedDescription() : "Không có mô tả");

            // Setup image viewpager
            setupImageViewPager();

            // ✅ SETUP BUY BUTTON
            setupBuyButton();

            Log.d(TAG, "Product data loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error loading product data", e);
            Toast.makeText(this, "Lỗi khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupImageViewPager() {
        List<String> images = product.getImages();

        if (images != null && !images.isEmpty()) {
            // Create adapter for image viewpager
            ProductImageAdapter imageAdapter = new ProductImageAdapter(images, this);
            viewPagerImages.setAdapter(imageAdapter);

            // Setup dots indicator if more than 1 image
            if (images.size() > 1) {
                tabLayoutDots.setVisibility(View.VISIBLE);
                new TabLayoutMediator(tabLayoutDots, viewPagerImages,
                        (tab, position) -> {
                            // Empty implementation - dots will be shown automatically
                        }).attach();
            } else {
                tabLayoutDots.setVisibility(View.GONE);
            }

            Log.d(TAG, "Image ViewPager setup with " + images.size() + " images");
        } else {
            Log.w(TAG, "No images available for product");
            tabLayoutDots.setVisibility(View.GONE);
        }
    }

    // ✅ SETUP BUY BUTTON BASED ON PRODUCT STATUS AND OWNERSHIP
    private void setupBuyButton() {
        if (product == null) {
            btnBuyNow.setVisibility(View.GONE);
            return;
        }

        String productStatus = product.getStatus();
        String productOwnerId = product.getUserId();

        Log.d(TAG, "Setting up buy button - Status: " + productStatus + ", Owner: " + productOwnerId + ", Current: " + currentUserId);

        // Check if current user is the owner of the product
        if (currentUserId.equals(productOwnerId)) {
            btnBuyNow.setVisibility(View.GONE);
            Log.d(TAG, "Hide buy button - user owns this product");
            return;
        }

        // Check product status
        if ("posting".equals(productStatus)) {
            // Product is available for purchase
            btnBuyNow.setVisibility(View.VISIBLE);
            btnBuyNow.setEnabled(true);
            btnBuyNow.setText("MUA NGAY");
//            btnBuyNow.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            Log.d(TAG, "Show buy button - product available");
        } else if ("waitingdelivery".equals(productStatus)) {
            // Product is being processed/delivered
            btnBuyNow.setVisibility(View.VISIBLE);
            btnBuyNow.setEnabled(false);
            btnBuyNow.setText("ĐANG GIAO HÀNG");
            btnBuyNow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            Log.d(TAG, "Show disabled buy button - product being delivered");
        } else {
            // Product not available (waitpass, rejectpass, etc.)
            btnBuyNow.setVisibility(View.GONE);
            Log.d(TAG, "Hide buy button - product not available");
        }
    }

    // ✅ HANDLE BUY NOW BUTTON CLICK
    private void handleBuyNowClick() {
        if (product == null) {
            Toast.makeText(this, "Lỗi: Thông tin sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to login screen
            return;
        }

        // Check if user is trying to buy their own product (extra safety check)
        if (currentUserId.equals(product.getUserId())) {
            Toast.makeText(this, "Bạn không thể mua sản phẩm của chính mình", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check product status
        if (!"posting".equals(product.getStatus())) {
            Toast.makeText(this, "Sản phẩm này hiện không có sẵn để mua", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to checkout page
        Intent checkoutIntent = CheckoutActivity.createIntent(this, product);
        startActivity(checkoutIntent);

        Log.d(TAG, "Navigating to checkout for product: " + product.getProductName());
    }

    private String getStatusDisplayText(String status) {
        if (status == null) return "Không xác định";

        switch (status.toLowerCase()) {
            case "waitpass":
                return "Chờ duyệt";
            case "rejectpass":
                return "Bị từ chối";
            case "posting":
                return "Đang bán";
            case "waitingdelivery":
                return "Đang giao hàng";
            case "delivered":
                return "Đã bán";
            default:
                return status;
        }
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;

        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        }

        // TODO: Call API to update favorite status
        Log.d(TAG, "Favorite status changed to: " + isFavorite);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Static method to create intent for this activity
    public static Intent createIntent(android.content.Context context, Product product) {
        Intent intent = new Intent(context, ChiTietSanPham.class);
        intent.putExtra(EXTRA_PRODUCT, product);
        return intent;
    }
}