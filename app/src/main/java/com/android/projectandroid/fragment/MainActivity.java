package com.android.projectandroid.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.productModel.GetProductsResponse;
import com.android.projectandroid.data.productModel.Product;
import com.android.projectandroid.data.productModel.ProductAdapter;
import com.android.projectandroid.user.CartActivity;
import com.android.projectandroid.user.ChiTietSanPham;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Fragment {

    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private TextView btnLoadMore;
    private SearchView searchView;
    private TextView tvNoProducts;
    private ImageView giohang;

    // Category items
    private LinearLayout categoryAo, categoryVay, categoryChannVay, categoryQuan;
    private LinearLayout categoryGiay, categoryNon, categoryPhuKien;

    // Category mappings - Danh mục chính và các danh mục con
    private static final List<String> CATEGORY_AO = Arrays.asList(
            "Áo sơ mi", "Áo thun", "Áo polo", "Áo hoodie", "Áo khoác", "Áo vest/blazer"
    );

    private static final List<String> CATEGORY_QUAN = Arrays.asList(
            "Quần jean", "Quần tây", "Quần short"
    );

    private static final List<String> CATEGORY_VAY = Arrays.asList(
            "Váy", "Đầm"
    );

    private static final List<String> CATEGORY_CHAN_VAY = Arrays.asList(
            "Chân váy"
    );

    private static final List<String> CATEGORY_GIAY = Arrays.asList(
            "Giày thể thao", "Giày cao gót", "Giày oxford", "Dép/sandal"
    );

    private static final List<String> CATEGORY_NON = Arrays.asList(
            "Mũ/nón"
    );

    private static final List<String> CATEGORY_PHU_KIEN = Arrays.asList(
            "Khăn", "Trang sức", "Đồng hồ", "Kính mát", "Thắt lưng",
            "Ví", "Ba lô", "Túi đeo chéo", "Túi xách tay"
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productList = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        // Đảm bảo padding cho view theo hệ thống (status bar, nav bar) động
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.trang_chu_chinh), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews(view);
        setupRecyclerView();
        setupLoadMoreButton();
        setupSearchView();
        setupCategoryClickListeners(view);
        loadSoldProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerProducts = view.findViewById(R.id.recycler_products);
        btnLoadMore = view.findViewById(R.id.btn_load_more);
        searchView = view.findViewById(R.id.open_search_bar_text_view);
        giohang = view.findViewById(R.id.gio_hang);

        giohang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CartActivity.class);
                startActivity(intent);
            }
        });
        // Create no products message TextView
        tvNoProducts = new TextView(getContext());
        tvNoProducts.setText("Không có sản phẩm nào khớp với tìm kiếm");
        tvNoProducts.setTextSize(16);
        tvNoProducts.setGravity(android.view.Gravity.CENTER);
        tvNoProducts.setPadding(32, 64, 32, 64);
        tvNoProducts.setVisibility(View.GONE);

        // Add to parent layout
        ViewGroup parent = (ViewGroup) recyclerProducts.getParent();
        parent.addView(tvNoProducts, parent.indexOfChild(recyclerProducts) + 1);
    }

    private void setupRecyclerView() {
        // Setup GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerProducts.setLayoutManager(gridLayoutManager);

        // Initialize adapter
        productAdapter = new ProductAdapter(productList, getContext());

        // Set click listener for products
        productAdapter.setOnProductClickListener(product -> {
            Log.d("MainActivity", "Product clicked: " + product.getProductName());
            navigateToProductDetail(product);
        });

        recyclerProducts.setAdapter(productAdapter);
    }
    private void navigateToProductDetail(Product product) {
        try {
            Log.d("MainActivity", "Navigating to product detail for: " + product.getProductName());

            // Create intent using the static method from ProductDetailActivity
            Intent intent = ChiTietSanPham.createIntent(getContext(), product);

            // Start the activity
            startActivity(intent);

        } catch (Exception e) {
            Log.e("MainActivity", "Error navigating to product detail", e);
            Toast.makeText(getContext(), "Không thể mở chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }
    private void setupSearchView() {
        if (searchView != null) {
            // Thiết lập placeholder text cho SearchView
            searchView.setQueryHint("Tìm kiếm sản phẩm theo tên...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("MainActivity", "Search submitted: " + query);

                    // Thực hiện tìm kiếm khi nhấn nút search
                    if (query != null && !query.trim().isEmpty()) {
                        performSearch(query.trim());
                    } else {
                        // Nếu query rỗng, clear search
                        clearSearch();
                    }

                    // Ẩn bàn phím sau khi search
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Không thực hiện tìm kiếm realtime nữa
                    // Chỉ clear search khi text hoàn toàn rỗng
                    if (newText.isEmpty()) {
                        clearSearch();
                    }
                    return true;
                }
            });

            // Handle search view close
            searchView.setOnCloseListener(() -> {
                clearSearch();
                return false;
            });

            // Handle search view expand/collapse
            searchView.setOnSearchClickListener(v -> {
                Log.d("MainActivity", "Search view expanded");
            });
        }
    }

    private void setupCategoryClickListeners(View view) {
        // Setup category click listeners với danh sách category cụ thể
        setupCategoryItem(view, R.id.category_ao, "Áo", CATEGORY_AO);
        setupCategoryItem(view, R.id.category_quan, "Quần", CATEGORY_QUAN);
        setupCategoryItem(view, R.id.category_vay, "Váy", CATEGORY_VAY);
        setupCategoryItem(view, R.id.category_chan_vay, "Chân váy", CATEGORY_CHAN_VAY);
        setupCategoryItem(view, R.id.category_giay, "Giày", CATEGORY_GIAY);
        setupCategoryItem(view, R.id.category_non, "Nón", CATEGORY_NON);
        setupCategoryItem(view, R.id.category_phu_kien, "Phụ kiện", CATEGORY_PHU_KIEN);
    }

    private void setupCategoryItem(View parentView, int categoryId, String categoryDisplayName, List<String> categoryList) {
        View categoryView = parentView.findViewById(categoryId);
        if (categoryView != null) {
            categoryView.setOnClickListener(v -> {
                Log.d("MainActivity", "Category clicked: " + categoryDisplayName);
                filterByCategories(categoryDisplayName, categoryList);
            });
        } else {
            Log.w("MainActivity", "Category view not found for: " + categoryDisplayName);
        }
    }

    private void performSearch(String query) {
        Log.d("MainActivity", "Performing search for: " + query);

        if (productAdapter != null) {
            // Tìm kiếm theo tên sản phẩm (không phải category)
            productAdapter.searchProducts(query);
            updateUIAfterFilter();

            // Show toast with search results
            int resultCount = productAdapter.getFilteredProductCount();
            if (resultCount > 0) {
                Toast.makeText(getContext(),
                        "Tìm thấy " + resultCount + " sản phẩm với từ khóa '" + query + "'",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),
                        "Không tìm thấy sản phẩm nào với từ khóa '" + query + "'",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void filterByCategories(String categoryDisplayName, List<String> categories) {
        Log.d("MainActivity", "Filtering by categories: " + categories.toString());

        if (productAdapter != null) {
            // Clear search view khi filter theo category
            if (searchView != null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
            }

            // Filter theo danh sách categories
            productAdapter.filterByCategories(categories);
            updateUIAfterFilter();

            // Show toast with filter results
            int resultCount = productAdapter.getFilteredProductCount();
            if (resultCount > 0) {
                Toast.makeText(getContext(),
                        "Tìm thấy " + resultCount + " sản phẩm trong danh mục " + categoryDisplayName,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(),
                        "Không có sản phẩm nào trong danh mục " + categoryDisplayName,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearSearch() {
        Log.d("MainActivity", "Clearing search and filters");

        if (productAdapter != null) {
            productAdapter.clearFilters();
            updateUIAfterFilter();

            Toast.makeText(getContext(),
                    "Hiển thị tất cả sản phẩm",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIAfterFilter() {
        if (productAdapter != null) {
            int filteredCount = productAdapter.getFilteredProductCount();
            int displayedCount = productAdapter.getDisplayedProductCount();

            Log.d("MainActivity", "Filter results - Filtered: " + filteredCount +
                    ", Displayed: " + displayedCount);

            // Show/hide no products message
            if (filteredCount == 0) {
                recyclerProducts.setVisibility(View.GONE);
                tvNoProducts.setVisibility(View.VISIBLE);
                btnLoadMore.setVisibility(View.GONE);
            } else {
                recyclerProducts.setVisibility(View.VISIBLE);
                tvNoProducts.setVisibility(View.GONE);
                updateLoadMoreButtonVisibility();
            }
        }
    }

    private void setupLoadMoreButton() {
        btnLoadMore.setOnClickListener(v -> {
            Log.d("MainActivity", "Load More button clicked");

            // Check if adapter exists and has data
            if (productAdapter == null) {
                Log.w("MainActivity", "ProductAdapter is null");
                return;
            }

            // Debug: Log current state
            Log.d("MainActivity", "Current displayed: " + productAdapter.getDisplayedProductCount() +
                    "/" + productAdapter.getFilteredProductCount() +
                    " (filtered from " + productAdapter.getTotalProductCount() + " total)");
            Log.d("MainActivity", "Has more products: " + productAdapter.hasMoreProducts());

            // Check if there are more products to load
            if (!productAdapter.hasMoreProducts()) {
                if (productAdapter.isFiltered()) {
                    Toast.makeText(getContext(), "Đã hiển thị tất cả kết quả tìm kiếm", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Đã hiển thị tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                }
                btnLoadMore.setVisibility(View.GONE);
                return;
            }

            // Disable button temporarily to prevent multiple clicks
            btnLoadMore.setEnabled(false);
            btnLoadMore.setText("ĐANG TẢI...");

            // Add small delay for better UX
            btnLoadMore.postDelayed(() -> {
                try {
                    // Load more products
                    boolean hasMore = productAdapter.loadMoreProducts();

                    if (hasMore) {
                        // Show success message
                        int newlyLoaded = Math.min(4, productAdapter.getFilteredProductCount() -
                                (productAdapter.getDisplayedProductCount() - 4));

                        Log.d("MainActivity", "Successfully loaded more products. New count: " +
                                productAdapter.getDisplayedProductCount() + "/" +
                                productAdapter.getFilteredProductCount());
                    } else {
                        // No more products to load
                        if (getContext() != null) {
                            if (productAdapter.isFiltered()) {
                                Toast.makeText(getContext(), "Đã hiển thị tất cả kết quả tìm kiếm", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Đã hiển thị tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Log.d("MainActivity", "No more products to load");
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error loading more products", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi khi tải thêm sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                }

                // Update button visibility and state
                updateLoadMoreButtonVisibility();

            }, 500);
        });
    }

    private void updateLoadMoreButtonVisibility() {
        if (productAdapter != null) {
            boolean hasMore = productAdapter.hasMoreProducts();
            int filteredCount = productAdapter.getFilteredProductCount();

            Log.d("MainActivity", "Updating button visibility. Has more: " + hasMore +
                    ", Filtered count: " + filteredCount);

            if (filteredCount == 0) {
                // No products to show
                btnLoadMore.setVisibility(View.GONE);
            } else if (hasMore) {
                btnLoadMore.setVisibility(View.VISIBLE);
                btnLoadMore.setEnabled(true);
                btnLoadMore.setText("XEM THÊM");
                Log.d("MainActivity", "Button shown - more products available");
            } else {
                btnLoadMore.setVisibility(View.GONE);
                Log.d("MainActivity", "Button hidden - no more products");
            }
        } else {
            Log.w("MainActivity", "ProductAdapter is null in updateLoadMoreButtonVisibility");
            btnLoadMore.setVisibility(View.GONE);
        }
    }

    private void loadSoldProducts() {
        Log.d("MainActivity", "Loading selling products...");

        // Call API to get products with "posting" status
        ApiService.apiService.getAllProducts("posting").enqueue(new Callback<GetProductsResponse>() {
            @Override
            public void onResponse(Call<GetProductsResponse> call, Response<GetProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetProductsResponse productsResponse = response.body();

                    if (productsResponse.isSuccess() && productsResponse.getData() != null) {
                        productList.clear();
                        productList.addAll(productsResponse.getData());

                        Log.d("MainActivity", "API returned " + productList.size() + " products");

                        // Update RecyclerView
                        if (isAdded() && getActivity() != null) {
                            // Update adapter with new data (this will reset pagination and filters)
                            productAdapter.updateProducts(productList);

                            // Update UI
                            updateUIAfterFilter();

                            Log.d("MainActivity", "Updated adapter. Total: " +
                                    productAdapter.getTotalProductCount() +
                                    ", Filtered: " + productAdapter.getFilteredProductCount() +
                                    ", Displayed: " + productAdapter.getDisplayedProductCount());
                        }

                    } else {
                        Log.w("MainActivity", "No products found or API returned unsuccessful response");
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                        }
                        showNoProductsState();
                    }
                } else {
                    Log.e("MainActivity", "Failed to load products: " + response.code());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                    showNoProductsState();
                }
            }

            @Override
            public void onFailure(Call<GetProductsResponse> call, Throwable throwable) {
                Log.e("MainActivity", "API call failed", throwable);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
                showNoProductsState();
            }
        });
    }

    private void showNoProductsState() {
        recyclerProducts.setVisibility(View.GONE);
        tvNoProducts.setVisibility(View.VISIBLE);
        btnLoadMore.setVisibility(View.GONE);
    }

    // Method to refresh products (can be called from other places if needed)
    public void refreshProducts() {
        // Clear search if active
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }

        loadSoldProducts();
    }

    // Method to load products with different status
    public void loadProductsByStatus(String status) {
        Log.d("MainActivity", "Loading products with status: " + status);

        ApiService.apiService.getAllProducts(status).enqueue(new Callback<GetProductsResponse>() {
            @Override
            public void onResponse(Call<GetProductsResponse> call, Response<GetProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetProductsResponse productsResponse = response.body();

                    if (productsResponse.isSuccess() && productsResponse.getData() != null) {
                        productList.clear();
                        productList.addAll(productsResponse.getData());

                        if (isAdded() && getActivity() != null) {
                            productAdapter.updateProducts(productList);
                            updateUIAfterFilter();
                        }

                        Log.d("MainActivity", "Loaded " + productList.size() + " products with status: " + status);
                    }
                } else {
                    Log.e("MainActivity", "Failed to load products: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetProductsResponse> call, Throwable throwable) {
                Log.e("MainActivity", "API call failed", throwable);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}