package com.android.projectandroid.user;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.CancelOrderRequest;
import com.android.projectandroid.data.orderModel.GetOrdersResponse;
import com.android.projectandroid.data.orderModel.ProductInfoResponse;
import com.android.projectandroid.data.orderModel.SimpleOrder;
import com.android.projectandroid.data.orderModel.SimpleOrderAdapter;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.orderModel.UpdateOrderResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersActivity extends AppCompatActivity implements SimpleOrderAdapter.OnOrderActionListener {

    private static final String TAG = "MyOrdersActivity";

    // UI Components
    private ImageButton btnBack;
    private TextView tvTitle;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerViewOrders;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;

    // Data
    private SimpleOrderAdapter orderAdapter;
    private List<SimpleOrder> orderList;
    private String currentStatusFilter = "all";

    // User info - ✅ KHÔNG CẦN AUTH TOKEN NỮA
    private String userId;

    // ✅ CACHE PRODUCT INFO ĐỂ TRÁNH GỌI API NHIỀU LẦN
    private Map<String, ProductInfoResponse.ProductInfo> productInfoCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_orders), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Get user info
        getUserInfo();

        // Initialize views
        initViews();

        // Setup views
        setupViews();

        // Load orders
        loadOrders();
    }

    // ✅ CẬP NHẬT: KHÔNG CẦN AUTH TOKEN
    private void getUserInfo() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "User ID: " + userId);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        chipGroupStatus = findViewById(R.id.chip_group_status);
        recyclerViewOrders = findViewById(R.id.recycler_view_orders);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupViews() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        setupRecyclerView();

        // ✅ SETUP STATUS FILTER CHIPS CHỈ 3 TRẠNG THÁI
        setupStatusChips();

        // Setup swipe refresh
        setupSwipeRefresh();
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new SimpleOrderAdapter(orderList, this);
        orderAdapter.setOnOrderActionListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewOrders.setLayoutManager(layoutManager);
        recyclerViewOrders.setAdapter(orderAdapter);

        Log.d(TAG, "RecyclerView setup completed");
    }

    // ✅ SETUP STATUS CHIPS CHỈ 4 OPTIONS (all + 3 trạng thái)
    private void setupStatusChips() {
        String[] statusFilters = {"all", "shipping", "delivered", "cancelled"};
        String[] statusLabels = {"Tất cả", "Đang giao", "Đã giao", "Đã hủy"};

        for (int i = 0; i < statusFilters.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(statusLabels[i]);
            chip.setTag(statusFilters[i]);
            chip.setCheckable(true);

            // Set first chip as checked
            if (i == 0) {
                chip.setChecked(true);
            }

            chip.setOnCheckedChangeListener((view, isChecked) -> {
                if (isChecked) {
                    String filter = (String) view.getTag();
                    currentStatusFilter = filter;
                    loadOrders();

                    // Uncheck other chips
                    for (int j = 0; j < chipGroupStatus.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroupStatus.getChildAt(j);
                        if (otherChip != view) {
                            otherChip.setChecked(false);
                        }
                    }
                }
            });

            chipGroupStatus.addView(chip);
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Refreshing orders...");
            productInfoCache.clear(); // Clear cache on refresh
            loadOrders();
        });

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
    }

    // ✅ CẬP NHẬT: LOAD ORDERS KHÔNG CẦN TOKEN
    private void loadOrders() {
        // Show loading
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        String statusParam = currentStatusFilter.equals("all") ? null : currentStatusFilter;

        Log.d(TAG, "Loading orders for user: " + userId + ", status: " + statusParam);

        SimpleOrderApiService.orderApiService.getUserOrders(userId, statusParam)
                .enqueue(new Callback<GetOrdersResponse>() {
                    @Override
                    public void onResponse(Call<GetOrdersResponse> call, Response<GetOrdersResponse> response) {
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            GetOrdersResponse ordersResponse = response.body();

                            if (ordersResponse.isSuccess() && ordersResponse.getData() != null) {
                                orderList.clear();
                                orderList.addAll(ordersResponse.getData());

                                orderAdapter.notifyDataSetChanged();
                                updateEmptyState();

                                Log.d(TAG, "Loaded " + orderList.size() + " orders from API");

                                // ✅ LOAD PRODUCT INFO CHO CÁC ORDERS
                                loadProductInfoForOrders();

                            } else {
                                Log.w(TAG, "API returned unsuccessful response: " + ordersResponse.getMessage());
                                handleEmptyOrders();
                            }
                        } else {
                            Log.e(TAG, "Failed to load orders: " + response.code() + " - " + response.message());
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error reading error body", e);
                                }
                            }
                            Toast.makeText(MyOrdersActivity.this,
                                    "Không thể tải đơn hàng: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            handleEmptyOrders();
                        }
                    }

                    @Override
                    public void onFailure(Call<GetOrdersResponse> call, Throwable throwable) {
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "API call failed", throwable);
                        Toast.makeText(MyOrdersActivity.this,
                                "Lỗi kết nối: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        handleEmptyOrders();
                    }
                });
    }

    private void handleEmptyOrders() {
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    // ✅ MỚI: LOAD PRODUCT INFO CHO TẤT CẢ ORDERS
    private void loadProductInfoForOrders() {
        for (SimpleOrder order : orderList) {
            String productId = order.getProductId(); // Bây giờ đây là String

            if (productId != null && !productId.isEmpty()) {
                // Kiểm tra cache trước
                if (productInfoCache.containsKey(productId)) {
                    Log.d(TAG, "Using cached product info for: " + productId);
                    continue;
                }

                // Load product info từ API
                loadProductInfo(productId);
            }
        }
    }

    // ✅ MỚI: LOAD PRODUCT INFO RIÊNG BIỆT
    private void loadProductInfo(String productId) {
        SimpleOrderApiService.orderApiService.getProductInfo(productId)
                .enqueue(new Callback<ProductInfoResponse>() {
                    @Override
                    public void onResponse(Call<ProductInfoResponse> call, Response<ProductInfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            ProductInfoResponse.ProductInfo productInfo = response.body().getData();

                            // Cache product info
                            productInfoCache.put(productId, productInfo);

                            Log.d(TAG, "Loaded product info for: " + productInfo.getProductName());

                            // Notify adapter to update UI (nếu cần)
                            // orderAdapter.notifyDataSetChanged();

                        } else {
                            Log.w(TAG, "Failed to load product info for: " + productId);
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductInfoResponse> call, Throwable t) {
                        Log.e(TAG, "Error loading product info for: " + productId, t);
                    }
                });
    }

    // ✅ MỚI: GET PRODUCT INFO FROM CACHE
    public ProductInfoResponse.ProductInfo getProductInfo(String productId) {
        return productInfoCache.get(productId);
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);

            // ✅ CUSTOM MESSAGE CHO TỪNG TRẠNG THÁI
            String emptyMessage = getEmptyStateMessage();
            tvEmptyState.setText(emptyMessage);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewOrders.setVisibility(View.VISIBLE);
        }
    }

    // ✅ CUSTOM EMPTY STATE MESSAGE CHO 3 TRẠNG THÁI
    private String getEmptyStateMessage() {
        switch (currentStatusFilter) {
            case "shipping":
                return "🚚 Bạn không có đơn hàng nào đang giao\n\nHãy đặt hàng để trải nghiệm dịch vụ của chúng tôi!";
            case "delivered":
                return "✅ Bạn chưa có đơn hàng nào đã giao\n\nCác đơn hàng hoàn thành sẽ xuất hiện ở đây.";
            case "cancelled":
                return "❌ Bạn không có đơn hàng nào đã hủy\n\nRất tốt! Điều này có nghĩa là bạn hài lòng với các đơn hàng.";
            default:
                return "📦 Bạn chưa có đơn hàng nào\n\nHãy khám phá và mua sắm những sản phẩm tuyệt vời!";
        }
    }

    // SimpleOrderAdapter.OnOrderActionListener implementation
    @Override
    public void onViewOrderDetail(SimpleOrder order) {
        Log.d(TAG, "View details for order: " + order.getId());

        // ✅ HIỂN THỊ CHI TIẾT ĐƠN HÀNG
        showOrderDetailsDialog(order);
    }

    @Override
    public void onCancelOrder(SimpleOrder order) {
        Log.d(TAG, "Cancel order: " + order.getId());

        // ✅ CHỈ CÓ THỂ HỦY KHI ĐANG GIAO
        if (!order.canCancel()) {
            Toast.makeText(this, "Chỉ có thể hủy đơn hàng đang giao", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?\n\n🚚 " + order.getProductName() +
                        "\n💰 " + order.getFormattedPrice())
                .setPositiveButton("Hủy đơn hàng", (dialog, which) -> {
                    cancelOrderAPI(order);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onContactSeller(SimpleOrder order) {
        Log.d(TAG, "Contact seller for order: " + order.getId());

        String message = "";
        switch (order.getStatus()) {
            case "shipping":
                message = "Liên hệ về đơn hàng đang giao";
                break;
            case "delivered":
                message = "Đánh giá đơn hàng đã giao";
                break;
            default:
                message = "Liên hệ về đơn hàng";
        }

        // TODO: Implement contact/rating functionality
        Toast.makeText(this, message + " - Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReorderProduct(SimpleOrder order) {
        Log.d(TAG, "Reorder product: " + order.getProductId());
        // TODO: Navigate back to product detail for reorder
        Toast.makeText(this, "Đặt lại sản phẩm - Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    // ✅ FIX: HIỂN THỊ CHI TIẾT ĐƠN HÀNG VỚI PRODUCT INFO - SỬ DỤNG FORMATTED METHODS
    private void showOrderDetailsDialog(SimpleOrder order) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chi tiết đơn hàng " + order.getStatusIcon());

        // ✅ SỬ DỤNG PRODUCT INFO TỪ CACHE HOẶC SNAPSHOT
        String productName = order.getProductName(); // Từ snapshot
        ProductInfoResponse.ProductInfo productInfo = getProductInfo(order.getProductId());
        if (productInfo != null) {
            productName = productInfo.getProductName(); // Real-time data
        }

        String details = "📦 Sản phẩm: " + productName + "\n" +
                "🆔 Mã sản phẩm: " + order.getProductId() + "\n" +
                "💰 Giá: " + order.getFormattedPrice() + "\n" +
                "📋 Trạng thái: " + order.getStatusDisplayText() + "\n" +
                "🚚 Loại: " + order.getOrderTypeDisplayText() + "\n" +
                "📅 Ngày đặt: " + order.getFormattedOrderDate(); // ✅ FIX: Sử dụng method có sẵn

        // ✅ THÔNG TIN CUSTOMER
        if (order.getCustomerInfo() != null) {
            details += "\n\n👤 Thông tin nhận hàng:\n" +
                    "• Tên: " + order.getCustomerName() + "\n" +
                    "• SĐT: " + order.getCustomerPhone() + "\n" +
                    "• Địa chỉ: " + order.getCustomerAddress();
        }

        // ✅ FIX: THÊM CHI TIẾT THEO TRẠNG THÁI - SỬ DỤNG FORMATTED METHODS
        switch (order.getStatus().toLowerCase()) {
            case "delivered":
                String deliveredTime = order.getFormattedDeliveredTime(); // ✅ FIX
                if (!deliveredTime.isEmpty()) {
                    details += "\n✅ Đã giao lúc: " + deliveredTime;
                }
                break;
            case "cancelled":
                String cancelledTime = order.getFormattedCancelledTime(); // ✅ FIX
                if (!cancelledTime.isEmpty()) {
                    details += "\n❌ Đã hủy lúc: " + cancelledTime;
                }
                if (order.getCancelReason() != null && !order.getCancelReason().isEmpty()) {
                    details += "\n🔍 Lý do hủy: " + order.getCancelReason();
                }
                break;
        }

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details += "\n📝 Ghi chú: " + order.getNotes();
        }

        // ✅ HIỂN THỊ PRODUCT INFO NẾU CÓ
        if (productInfo != null) {
            details += "\n\n📊 Thông tin sản phẩm:";
            details += "\n• Danh mục: " + productInfo.getCategory();
            details += "\n• Trạng thái: " + productInfo.getStatus();
            if (productInfo.getImages() != null && !productInfo.getImages().isEmpty()) {
                details += "\n• Số ảnh: " + productInfo.getImages().size();
            }
        }

        builder.setMessage(details);
        builder.setPositiveButton("Đóng", null);

        // ✅ THÊM BUTTON ACTIONS THEO TRẠNG THÁI
        if (order.canCancel()) {
            builder.setNeutralButton("Hủy đơn", (dialog, which) -> {
                onCancelOrder(order);
            });
        }

        builder.show();
    }

    // ✅ CẬP NHẬT: CANCEL ORDER KHÔNG CẦN TOKEN
    private void cancelOrderAPI(SimpleOrder order) {
        String reason = "Khách hàng yêu cầu hủy";
        CancelOrderRequest request = new CancelOrderRequest(reason, userId);

        SimpleOrderApiService.orderApiService.cancelOrder(order.getId(), request)
                .enqueue(new Callback<UpdateOrderResponse>() {
                    @Override
                    public void onResponse(Call<UpdateOrderResponse> call, Response<UpdateOrderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UpdateOrderResponse updateResponse = response.body();

                            if (updateResponse.isSuccess()) {
                                // Update local order
                                order.setStatus("cancelled");
                                // ✅ FIX: Set cancelled time as string
                                order.setCancelledAt(getCurrentTimestamp());
                                order.setCancelReason(reason);

                                orderAdapter.notifyDataSetChanged();

                                Toast.makeText(MyOrdersActivity.this, "✅ Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Order cancelled successfully: " + order.getId());

                                // Reload orders to get fresh data
                                loadOrders();
                            } else {
                                Toast.makeText(MyOrdersActivity.this, "❌ Lỗi: " + updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MyOrdersActivity.this, "❌ Không thể hủy đơn hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Cancel order failed: " + response.code() + " - " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateOrderResponse> call, Throwable throwable) {
                        Log.e(TAG, "Failed to cancel order", throwable);
                        Toast.makeText(MyOrdersActivity.this, "❌ Lỗi kết nối khi hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ HELPER METHOD: GET CURRENT TIMESTAMP AS STRING
    private String getCurrentTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders when activity resumes
        loadOrders();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // ✅ PUBLIC METHOD ĐỂ FILTER THEO STATUS
    public void filterByStatus(String status) {
        if (status != null && (status.equals("all") || status.equals("shipping") ||
                status.equals("delivered") || status.equals("cancelled"))) {
            currentStatusFilter = status;
            loadOrders();

            // Update UI to reflect filter
            for (int i = 0; i < chipGroupStatus.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupStatus.getChildAt(i);
                chip.setChecked(status.equals(chip.getTag()));
            }
        }
    }

    // ✅ MỚI: CLEAR PRODUCT CACHE
    public void clearProductCache() {
        productInfoCache.clear();
        Log.d(TAG, "Product info cache cleared");
    }

    // ✅ MỚI: GET ORDER BY ID
    public SimpleOrder getOrderById(String orderId) {
        for (SimpleOrder order : orderList) {
            if (order.getId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }
}