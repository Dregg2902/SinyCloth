package com.android.projectandroid.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.cartModel.CartAdapter;
import com.android.projectandroid.data.cartModel.CartManager;
import com.android.projectandroid.data.productModel.GetProductsResponse;
import com.android.projectandroid.data.productModel.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {

    private static final String TAG = "CartActivity";

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private TextView tvEmptyCart;
    private TextView tvCartTitle;
    private ImageView btnBack;

    private CartManager cartManager;
    private List<Product> cartItems;
    private boolean isLoadingFromServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Load và sync dữ liệu từ server
        loadAndSyncCartItems();
    }

    private void initViews() {
        recyclerCart = findViewById(R.id.recycler_cart);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        tvCartTitle = findViewById(R.id.tv_cart_title);
        btnBack = findViewById(R.id.btn_back);

        cartManager = CartManager.getInstance(this);
    }

    private void setupRecyclerView() {
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Load cart items và sync với server để có trạng thái mới nhất
     */
    private void loadAndSyncCartItems() {
        // Hiển thị cart local trước để UX tốt hơn
        loadCartItemsFromLocal();

        // Sau đó sync với server để cập nhật trạng thái
        syncCartWithServer();
    }

    /**
     * Load cart items từ local storage
     */
    private void loadCartItemsFromLocal() {
        cartItems = cartManager.getCartItems();

        Log.d(TAG, "Loading cart items from local: " + cartItems.size() + " products");

        if (cartItems.isEmpty()) {
            showEmptyCartState();
        } else {
            showCartItems();
        }

        updateCartTitle();
    }

    /**
     * Sync cart với server để cập nhật trạng thái sản phẩm mới nhất
     */
    private void syncCartWithServer() {
        List<Product> localCartItems = cartManager.getCartItems();

        if (localCartItems.isEmpty()) {
            Log.d(TAG, "No items in cart to sync");
            return;
        }

        if (isLoadingFromServer) {
            Log.d(TAG, "Already syncing with server");
            return;
        }

        isLoadingFromServer = true;
        Log.d(TAG, "Syncing cart with server to get latest product status");

        // Gọi API để lấy tất cả sản phẩm có status posting (có thể mua)
        ApiService.apiService.getAllProducts(null)
                .enqueue(new Callback<GetProductsResponse>() {
                    @Override
                    public void onResponse(Call<GetProductsResponse> call, Response<GetProductsResponse> response) {
                        isLoadingFromServer = false;

                        if (response.isSuccessful() && response.body() != null) {
                            GetProductsResponse serverResponse = response.body();

                            if (serverResponse.isSuccess() && serverResponse.getData() != null) {
                                List<Product> serverProducts = serverResponse.getData();
                                updateCartWithServerData(localCartItems, serverProducts);
                                Log.d(TAG, "Successfully synced cart with server data");
                            } else {
                                Log.w(TAG, "Server response unsuccessful: " + serverResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Failed to sync with server: " + response.code() + " - " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<GetProductsResponse> call, Throwable throwable) {
                        isLoadingFromServer = false;
                        Log.e(TAG, "Network error when syncing cart", throwable);
                        // Không hiển thị error cho user vì đã có data local
                    }
                });
    }

    /**
     * Cập nhật cart với data từ server
     */
    private void updateCartWithServerData(List<Product> localCartItems, List<Product> serverProducts) {
        boolean hasUpdates = false;
        List<Product> updatedCartItems = new ArrayList<>();

        for (Product localProduct : localCartItems) {
            Product serverProduct = findProductInServerList(localProduct.getId(), serverProducts);

            if (serverProduct != null) {
                // Sản phẩm tồn tại trên server - cập nhật thông tin mới nhất
                if (!localProduct.getStatus().equals(serverProduct.getStatus())) {
                    Log.d(TAG, "Product status changed: " + localProduct.getProductName() +
                            " from " + localProduct.getStatus() + " to " + serverProduct.getStatus());
                    hasUpdates = true;
                }

                // Sử dụng data từ server (có thông tin mới nhất)
                updatedCartItems.add(serverProduct);

                // Cập nhật trong CartManager
                cartManager.updateProductInCart(serverProduct);
            } else {
                // Sản phẩm không tồn tại trên server - có thể đã bị xóa
                Log.w(TAG, "Product not found on server: " + localProduct.getProductName());

                // Tạo bản copy với status "deleted" để hiển thị
                Product deletedProduct = createDeletedProduct(localProduct);
                updatedCartItems.add(deletedProduct);
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            // Cập nhật UI với data mới
            cartItems = updatedCartItems;
            runOnUiThread(() -> {
                if (cartItems.isEmpty()) {
                    showEmptyCartState();
                } else {
                    showCartItems();
                }
                updateCartTitle();

                // Thông báo cho user nếu có sản phẩm thay đổi trạng thái
                Toast.makeText(this, "Đã cập nhật trạng thái sản phẩm mới nhất", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Tạo bản copy của product với status "deleted"
     */
    private Product createDeletedProduct(Product originalProduct) {
        Product deletedProduct = new Product();
        deletedProduct.setId(originalProduct.getId());
        deletedProduct.setProductName(originalProduct.getProductName());
        deletedProduct.setImages(originalProduct.getImages());
        deletedProduct.setCategory(originalProduct.getCategory());
        deletedProduct.setOriginalPrice(originalProduct.getOriginalPrice());
        deletedProduct.setCondition(originalProduct.getCondition());
        deletedProduct.setPurchasePrice(originalProduct.getPurchasePrice());
        deletedProduct.setSellPrice(originalProduct.getSellPrice());
        deletedProduct.setShortDescription(originalProduct.getShortDescription());
        deletedProduct.setDetailedDescription(originalProduct.getDetailedDescription());
        deletedProduct.setUserId(originalProduct.getUserId());
        deletedProduct.setUser(originalProduct.getUser());
        deletedProduct.setStatus("deleted"); // Đánh dấu đã bị xóa
        deletedProduct.setCreatedAt(originalProduct.getCreatedAt());
        deletedProduct.setUpdatedAt(originalProduct.getUpdatedAt());
        deletedProduct.setRejectionReason(originalProduct.getRejectionReason());

        return deletedProduct;
    }

    /**
     * Tìm sản phẩm trong danh sách server theo ID
     */
    private Product findProductInServerList(String productId, List<Product> serverProducts) {
        for (Product serverProduct : serverProducts) {
            if (serverProduct.getId().equals(productId)) {
                return serverProduct;
            }
        }
        return null;
    }

    private void showEmptyCartState() {
        recyclerCart.setVisibility(View.GONE);
        tvEmptyCart.setVisibility(View.VISIBLE);
        tvEmptyCart.setText("Giỏ hàng của bạn đang trống\nHãy thêm sản phẩm yêu thích!");
    }

    private void showCartItems() {
        recyclerCart.setVisibility(View.VISIBLE);
        tvEmptyCart.setVisibility(View.GONE);

        if (cartAdapter == null) {
            cartAdapter = new CartAdapter(cartItems, this);
            cartAdapter.setOnCartItemActionListener(this);
            recyclerCart.setAdapter(cartAdapter);
        } else {
            cartAdapter.updateCartItems(cartItems);
        }
    }

    private void updateCartTitle() {
        int totalItems = cartItems.size();
        int availableItems = 0;

        // Đếm số sản phẩm có thể mua được (status = "posting")
        for (Product product : cartItems) {
            if ("posting".equals(product.getStatus())) {
                availableItems++;
            }
        }

        if (totalItems == 0) {
            tvCartTitle.setText("Giỏ hàng");
        } else if (availableItems == totalItems) {
            // Tất cả sản phẩm đều có thể mua
            tvCartTitle.setText("Giỏ hàng (" + totalItems + ")");
        } else {
            // Có sản phẩm hết hàng
            tvCartTitle.setText("Giỏ hàng (" + availableItems + "/" + totalItems + " có sẵn)");
        }

        Log.d(TAG, "Cart title updated - Total: " + totalItems + ", Available: " + availableItems);
    }

    @Override
    public void onRemoveFromCart(Product product, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khỏi giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + product.getProductName() + "\" khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa sản phẩm khỏi CartManager
                    boolean removed = cartManager.removeFromCart(product.getId());
                    if (removed) {
                        // Load lại từ local và sync với server
                        loadAndSyncCartItems();

                        Toast.makeText(this, "Đã xóa \"" + product.getProductName() + "\" khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Removed product from cart: " + product.getProductName());
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to remove product: " + product.getProductName());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onPurchaseProduct(Product product, int position) {
        // Kiểm tra trạng thái sản phẩm trước khi mua
        if (!"posting".equals(product.getStatus())) {
            String message = getStatusMessage(product.getStatus());
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Mua hàng")
                .setMessage("Bạn muốn mua \"" + product.getProductName() + "\"?")
                .setPositiveButton("Mua ngay", (dialog, which) -> {
                    // Chuyển đến CheckoutActivity để mua hàng
                    navigateToCheckout(product);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Lấy thông báo phù hợp theo trạng thái sản phẩm
     */
    private String getStatusMessage(String status) {
        switch (status) {
            case "sold":
                return "Sản phẩm này đã được bán cho khách khác";
            case "waitingdelivery":
                return "Sản phẩm này đang được giao hàng cho khách khác";
            case "delivered":
                return "Sản phẩm này đã được giao thành công";
            case "waitpass":
                return "Sản phẩm này đang chờ duyệt từ admin";
            case "rejectpass":
                return "Sản phẩm này đã bị từ chối bởi admin";
            case "deleted":
                return "Sản phẩm này không còn tồn tại trên hệ thống";
            default:
                return "Sản phẩm này hiện không có sẵn để mua";
        }
    }

    @Override
    public void onProductClick(Product product) {
        // Chỉ cho phép xem chi tiết nếu sản phẩm có sẵn
        if (!"posting".equals(product.getStatus())) {
            String message = getStatusMessage(product.getStatus());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        // Mở chi tiết sản phẩm
        Intent intent = ChiTietSanPham.createIntent(this, product);
        startActivity(intent);
    }

    /**
     * Chuyển đến màn hình CheckoutActivity để thực hiện mua hàng
     */
    private void navigateToCheckout(Product product) {
        try {
            // Sử dụng createIntentFromCart để đánh dấu đến từ giỏ hàng
            Intent intent = CheckoutActivity.createIntentFromCart(this, product);
            startActivity(intent);
            Log.d(TAG, "Navigating to checkout for product: " + product.getProductName());
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to checkout", e);
            Toast.makeText(this, "Lỗi khi chuyển đến trang đặt hàng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync lại với server khi quay lại màn hình
        Log.d(TAG, "onResume - Syncing cart with server");
        loadAndSyncCartItems();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "CartActivity destroyed");
    }
}