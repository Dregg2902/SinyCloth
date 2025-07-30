package com.android.projectandroid.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.CreateOrderRequest;
import com.android.projectandroid.data.orderModel.CreateOrderResponse;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.userModel.AddPointsRequest;
import com.android.projectandroid.data.userModel.AddPointsResponse;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.data.userModel.UserInfoResponse;
import com.android.projectandroid.data.userModel.UpdateProfileRequest;
import com.android.projectandroid.data.userModel.UpdateProfileResponse;
import com.android.projectandroid.data.productModel.Product;
import com.android.projectandroid.data.cartModel.CartManager;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    public static final String EXTRA_PRODUCT = "extra_product";
    public static final String EXTRA_FROM_CART = "extra_from_cart";

    // Views
    private ImageButton btnBack;
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductCategory;
    private TextView tvUserName;
    private EditText etUserPhone;
    private EditText etUserAddress;
    private RadioGroup rgOrderType;
    private EditText etNotes;
    private TextView btnPlaceOrder;
    private TextView tvOrderTypeDescription;

    // ✅ POINTS RELATED VIEWS
    private TextView tvAvailablePoints;
    private EditText etPointsToUse;
    private TextView btnUseAllPoints;
    private LinearLayout layoutDiscountInfo;
    private TextView tvPointsDiscount;
    private LinearLayout layoutOriginalPrice;
    private TextView tvSummaryOriginalPrice;
    private LinearLayout layoutPointsDiscountSummary;
    private TextView tvSummaryPointsDiscountValue;
    private TextView tvSummaryFinalPrice;

    // Data
    private Product product;
    private String userId;
    private DecimalFormat formatter;
    private PreferenceManager prefs;
    private CartManager cartManager;
    private boolean isFromCart = false;
    private double originalPrice = 0;
    private double finalPrice = 0;
    private double discountAmount = 0;

    // ✅ POINTS DATA
    private int availablePoints = 0;
    private int pointsToUse = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = new PreferenceManager(getBaseContext());
        cartManager = CartManager.getInstance(this);
        formatter = new DecimalFormat("#,###");

        // Get product from intent
        getProductFromIntent();

        // Get user info
        getUserInfo();

        // Initialize views
        initViews();

        // Setup views
        setupViews();

        // Load data
        loadProductData();
        loadUserData();

        // ✅ LOAD USER POINTS
        loadUserPoints();
    }

    private void getProductFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PRODUCT)) {
            product = (Product) intent.getSerializableExtra(EXTRA_PRODUCT);
            isFromCart = intent.getBooleanExtra(EXTRA_FROM_CART, false);
            Log.d(TAG, "Received product: " + (product != null ? product.getProductName() : "null") +
                    ", from cart: " + isFromCart);
        }

        if (product == null) {
            Log.e(TAG, "No product data received");
            Toast.makeText(this, "Lỗi: Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getUserInfo() {
        userId = prefs.getUserId();

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.d(TAG, "User ID: " + userId);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductCategory = findViewById(R.id.tv_product_category);
        tvUserName = findViewById(R.id.tv_user_name);
        etUserPhone = findViewById(R.id.et_user_phone);
        etUserAddress = findViewById(R.id.et_user_address);
        rgOrderType = findViewById(R.id.rg_order_type);
        etNotes = findViewById(R.id.et_notes);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        tvOrderTypeDescription = findViewById(R.id.tv_order_type_description);

        // ✅ INITIALIZE POINTS VIEWS
        tvAvailablePoints = findViewById(R.id.tv_available_points);
        etPointsToUse = findViewById(R.id.et_points_to_use);
        btnUseAllPoints = findViewById(R.id.btn_use_all_points);
        layoutDiscountInfo = findViewById(R.id.layout_discount_info);
        tvPointsDiscount = findViewById(R.id.tv_points_discount);
        layoutOriginalPrice = findViewById(R.id.layout_original_price);
        tvSummaryOriginalPrice = findViewById(R.id.tv_summary_original_price);
        layoutPointsDiscountSummary = findViewById(R.id.layout_points_discount);
        tvSummaryPointsDiscountValue = findViewById(R.id.tv_summary_points_discount);
        tvSummaryFinalPrice = findViewById(R.id.tv_summary_final_price);
    }

    private void setupViews() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Order type radio group
        rgOrderType.setOnCheckedChangeListener((group, checkedId) -> {
            updateOrderTypeDescription(checkedId);
        });

        // Place order button
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());

        // Set default order type
        rgOrderType.check(R.id.rb_delivery);
        updateOrderTypeDescription(R.id.rb_delivery);

        // ✅ SETUP POINTS FUNCTIONALITY
        setupPointsViews();
    }

    // ✅ SETUP POINTS RELATED VIEWS
    private void setupPointsViews() {
        // Points input text watcher
        etPointsToUse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculatePriceWithPoints();
            }
        });

        // Use all points button
        btnUseAllPoints.setOnClickListener(v -> {
            int maxUsablePoints = Math.min(availablePoints, (int) originalPrice);
            etPointsToUse.setText(String.valueOf(maxUsablePoints));
        });
    }

    // ✅ LOAD USER POINTS FROM SERVER
    private void loadUserPoints() {
        // Cập nhật UI
        NormalUser currentuser = prefs.getCurrentUser();
        availablePoints = currentuser.getPoints();
        tvAvailablePoints.setText(formatter.format(availablePoints) + " điểm");

        Log.d(TAG, "Loaded user points from preferences: " + availablePoints);
    }

    // ✅ CALCULATE PRICE WITH POINTS DISCOUNT
    private void calculatePriceWithPoints() {
        String pointsStr = etPointsToUse.getText().toString().trim();

        if (pointsStr.isEmpty()) {
            pointsToUse = 0;
        } else {
            try {
                pointsToUse = Integer.parseInt(pointsStr);

                // Validate points input
                if (pointsToUse < 0) {
                    pointsToUse = 0;
                    etPointsToUse.setText("0");
                } else if (pointsToUse > availablePoints) {
                    pointsToUse = availablePoints;
                    etPointsToUse.setText(String.valueOf(availablePoints));
                    Toast.makeText(this, "Không đủ điểm! Tối đa: " + availablePoints + " điểm", Toast.LENGTH_SHORT).show();
                } else if (pointsToUse > originalPrice) {
                    pointsToUse = (int) originalPrice;
                    etPointsToUse.setText(String.valueOf(pointsToUse));
                    Toast.makeText(this, "Điểm sử dụng không thể vượt quá giá sản phẩm!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                pointsToUse = 0;
            }
        }

        // Calculate discount and final price
        discountAmount = 4*pointsToUse; // 1 point = 1 VND
        finalPrice = Math.max(0, originalPrice - discountAmount);

        // Update UI
        updatePriceDisplay();

        Log.d(TAG, String.format("Price calculation - Original: %.0f, Points: %d, Discount: %.0f, Final: %.0f",
                originalPrice, pointsToUse, discountAmount, finalPrice));
    }

    // ✅ UPDATE PRICE DISPLAY
    private void updatePriceDisplay() {
        // Update discount info section
        if (pointsToUse > 0) {
            layoutDiscountInfo.setVisibility(View.VISIBLE);
            tvPointsDiscount.setText("-" + formatter.format(discountAmount) + " VNĐ");
        } else {
            layoutDiscountInfo.setVisibility(View.GONE);
        }

        // Update summary section
        if (pointsToUse > 0) {
            // Show original price
            layoutOriginalPrice.setVisibility(View.VISIBLE);
            tvSummaryOriginalPrice.setText(formatter.format(originalPrice) + " VNĐ");

            // Show points discount
            layoutPointsDiscountSummary.setVisibility(View.VISIBLE);
            tvSummaryPointsDiscountValue.setText("-" + formatter.format(discountAmount) + " VNĐ");
        } else {
            layoutOriginalPrice.setVisibility(View.GONE);
            layoutPointsDiscountSummary.setVisibility(View.GONE);
        }

        // Update final price
        tvSummaryFinalPrice.setText(formatter.format(finalPrice) + " VNĐ");

        // Update main product price display
        if (pointsToUse > 0) {
            tvProductPrice.setText(formatter.format(finalPrice) + " VNĐ");
        } else {
            tvProductPrice.setText(formatter.format(originalPrice) + " VNĐ");
        }
    }

    private void updateOrderTypeDescription(int checkedId) {
        String description = "";
        if (checkedId == R.id.rb_delivery) {
            description = "🚚 Shipper sẽ giao sản phẩm trực tiếp đến địa chỉ của bạn";
        }
        tvOrderTypeDescription.setText(description);
    }

    private void loadProductData() {
        if (product == null) return;

        try {
            // Product name
            if (product.getProductName() != null) {
                tvProductName.setText(product.getProductName());
            }

            // ✅ CALCULATE ORIGINAL PRICE
            originalPrice = product.getSellPrice() > 0 ? product.getSellPrice() : product.getPurchasePrice();
            finalPrice = originalPrice; // Initialize final price

            // Update price display
            tvProductPrice.setText(formatter.format(originalPrice) + " VNĐ");
            tvSummaryFinalPrice.setText(formatter.format(originalPrice) + " VNĐ");

            // Product category
            tvProductCategory.setText(product.getCategory() != null ? product.getCategory() : "Chưa phân loại");

            // Product image
            if (product.getImages() != null && !product.getImages().isEmpty() && product.getImages().get(0) != null) {
                Glide.with(this)
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.warnning_red_2)
                        .centerCrop()
                        .into(ivProductImage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading product data", e);
            Toast.makeText(this, "Lỗi khi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        String username = prefs.getUsername();
        String phone = prefs.getPhoneNumber();
        String address = prefs.getAddress();

        tvUserName.setText(username.isEmpty() ? "Người dùng" : username);
        etUserPhone.setText(phone);
        etUserAddress.setText(address);

        Log.d(TAG, "User data loaded - Phone: " + (!phone.isEmpty()) + ", Address: " + (!address.isEmpty()));
    }

    private void validateAndPlaceOrder() {
        // Validate phone number
        String phone = etUserPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            etUserPhone.setError("Vui lòng nhập số điện thoại");
            etUserPhone.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etUserPhone.setError("Số điện thoại không hợp lệ");
            etUserPhone.requestFocus();
            return;
        }

        // Validate address
        String address = etUserAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            etUserAddress.setError("Vui lòng nhập địa chỉ");
            etUserAddress.requestFocus();
            return;
        }

        if (address.length() < 10) {
            etUserAddress.setError("Địa chỉ quá ngắn");
            etUserAddress.requestFocus();
            return;
        }

        // Get order type
        String orderType = getSelectedOrderType();
        if (orderType == null) {
            Toast.makeText(this, "Vui lòng chọn loại đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get notes
        String notes = etNotes.getText().toString().trim();

        // Show confirmation dialog
        showConfirmationDialog(phone, address, orderType, notes);
    }

    private String getSelectedOrderType() {
        int checkedId = rgOrderType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_delivery) {
            return "delivery";
        }
        return null;
    }

    private void showConfirmationDialog(String phone, String address, String orderType, String notes) {
        String orderTypeText = "";
        switch (orderType) {
            case "pass_pickup":
                orderTypeText = "Đi lấy đồ pass";
                break;
            case "donation_pickup":
                orderTypeText = "Đi lấy đồ quyên góp";
                break;
            case "delivery":
                orderTypeText = "Đi giao hàng";
                break;
        }

        StringBuilder message = new StringBuilder();
        message.append("Xác nhận đặt hàng:\n\n")
                .append("📦 Sản phẩm: ").append(product.getProductName()).append("\n")
                .append("💰 Giá gốc: ").append(formatter.format(originalPrice)).append(" VNĐ\n");

        // ✅ SHOW POINTS DISCOUNT IN CONFIRMATION
        if (pointsToUse > 0) {
            message.append("🎯 Điểm sử dụng: ").append(formatter.format(pointsToUse)).append(" điểm\n")
                    .append("💸 Giảm giá: -").append(formatter.format(discountAmount)).append(" VNĐ\n")
                    .append("💵 Thành tiền: ").append(formatter.format(finalPrice)).append(" VNĐ\n");
        }

        message.append("🚚 Loại: ").append(orderTypeText).append("\n")
                .append("📞 SĐT: ").append(phone).append("\n")
                .append("📍 Địa chỉ: ").append(address);

        if (!notes.isEmpty()) {
            message.append("\n📝 Ghi chú: ").append(notes);
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage(message.toString())
                .setPositiveButton("Đặt hàng", (dialog, which) -> {
                    placeOrder(orderType, notes);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void placeOrder(String orderType, String notes) {
        // Disable button to prevent multiple clicks
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("ĐANG ĐẶT HÀNG...");

        String userId = prefs.getUserId();
        String kg = "0";

        if (userId == null || userId.isEmpty()) {
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText("ĐẶT HÀNG");
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ CREATE ORDER REQUEST - SỬ DỤNG FINAL PRICE (GIÁ SAU KHI ÁP ĐIỂM)
        CreateOrderRequest request = new CreateOrderRequest(userId, product.getId(), orderType, notes, kg, finalPrice);

        Log.d(TAG, String.format("Creating order - UserId: %s, ProductId: %s, Type: %s, Original Price: %.0f, Points Used: %d, Final Price: %.0f",
                userId, product.getId(), orderType, originalPrice, pointsToUse, finalPrice));

        SimpleOrderApiService.orderApiService.createOrder(request)
                .enqueue(new Callback<CreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("ĐẶT HÀNG");

                        if (response.isSuccessful() && response.body() != null) {
                            CreateOrderResponse orderResponse = response.body();

                            if (orderResponse != null && orderResponse.isSuccess()) {
                                Log.d(TAG, "Order created successfully with final price: " + finalPrice);

                                // ✅ UPDATE USER POINTS AFTER SUCCESSFUL ORDER
                                if (pointsToUse > 0) {
                                    updateUserPointsAfterOrder();
                                }

                                // Remove from cart if from cart
                                if (isFromCart && cartManager != null) {
                                    boolean removed = cartManager.removeFromCart(product.getId());
                                    Log.d(TAG, "Removed from cart after successful order: " + removed);
                                }

                                showSuccessDialog();
                            } else {
                                Log.e(TAG, "Order creation failed: " + (orderResponse != null ? orderResponse.getMessage() : "null response"));
                                Toast.makeText(CheckoutActivity.this, "Lỗi: " + (orderResponse != null ? orderResponse.getMessage() : "Server error"), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e(TAG, "Order creation failed: " + response.code() + " - " + response.message());
                            handleOrderError(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call, Throwable throwable) {
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText("ĐẶT HÀNG");

                        Log.e(TAG, "Order creation failed", throwable);
                        Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ UPDATE USER POINTS AFTER SUCCESSFUL ORDER
    private void updateUserPointsAfterOrder() {
        if (pointsToUse <= 0) return;

        int newPoints = availablePoints - pointsToUse;

        // Create request to update user points
        AddPointsRequest request = new AddPointsRequest();
        request.setUserId(userId);
        request.setPoints(newPoints);
        request.setType();

        ApiService.apiService.updatepoint(request)
                .enqueue(new Callback<AddPointsResponse>() {
                    @Override
                    public void onResponse(Call<AddPointsResponse> call, Response<AddPointsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AddPointsResponse updateResponse = response.body();
                            if (updateResponse.isSuccess()) {
                                prefs.updatePoints(updateResponse.getCurrentPoints());
                                ; // Save to preferences if you have this method

                                Log.d(TAG, String.format("Points updated successfully - Used: %d, Remaining: %d", pointsToUse, newPoints));
                            } else {
                                Log.e(TAG, "Failed to update points: " + updateResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Failed to update points: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<AddPointsResponse> call, Throwable t) {
                        Log.e(TAG, "Error updating points", t);
                    }
                });
    }

    private void handleOrderError(int code) {
        String errorMessage = "Không thể đặt hàng";
        if (code == 400) {
            errorMessage = "Thông tin đặt hàng không hợp lệ";
        } else if (code == 404) {
            errorMessage = "Sản phẩm không tồn tại";
        } else if (code >= 500) {
            errorMessage = "Lỗi máy chủ. Vui lòng thử lại sau";
        }

        Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showSuccessDialog() {
        StringBuilder successMessage = new StringBuilder();
        successMessage.append("Đơn hàng của bạn đã được tạo thành công.\n\n");

        // ✅ SHOW POINTS USED IN SUCCESS MESSAGE
        if (pointsToUse > 0) {
            successMessage.append("🎯 Đã sử dụng: ").append(formatter.format(pointsToUse)).append(" điểm\n")
                    .append("💰 Tiết kiệm: ").append(formatter.format(discountAmount)).append(" VNĐ\n")
                    .append("💵 Tổng thanh toán: ").append(formatter.format(finalPrice)).append(" VNĐ\n\n");
        }

        successMessage.append("Bạn có thể theo dõi trạng thái đơn hàng trong mục \"Đơn hàng của tôi\".");

        if (isFromCart) {
            successMessage.append("\n\nSản phẩm đã được tự động xóa khỏi giỏ hàng.");
        }

        new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công! 🎉")
                .setMessage(successMessage.toString())
                .setPositiveButton("Xem đơn hàng", (dialog, which) -> {
                    Intent intent = new Intent(this, MyOrdersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(isFromCart ? "Về giỏ hàng" : "Về trang chủ", (dialog, which) -> {
                    if (isFromCart) {
                        Intent intent = new Intent(this, CartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Static methods remain the same
    public static Intent createIntent(android.content.Context context, Product product) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_PRODUCT, product);
        return intent;
    }

    public static Intent createIntentFromCart(android.content.Context context, Product product) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_PRODUCT, product);
        intent.putExtra(EXTRA_FROM_CART, true);
        return intent;
    }
}