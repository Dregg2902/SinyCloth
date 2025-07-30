package com.android.projectandroid.data.productModel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.cartModel.CartManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";
    private List<Product> allProducts; // Tất cả sản phẩm từ API
    private List<Product> filteredProducts; // Sản phẩm sau khi filter
    private List<Product> displayedProducts; // Sản phẩm hiển thị trên UI
    private Context context;
    private OnProductClickListener onProductClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;
    private DecimalFormat formatter;
    private CartManager cartManager; // Thêm CartManager

    private static final int ITEMS_PER_PAGE = 4; // Số sản phẩm mỗi lần load
    private int currentPage = 0;
    private String currentSearchQuery = "";
    private List<String> currentCategories = new ArrayList<>(); // Thay đổi từ String thành List<String>

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, int position);
    }

    public ProductAdapter(List<Product> productList, Context context) {
        this.allProducts = productList != null ? new ArrayList<>(productList) : new ArrayList<>();
        this.filteredProducts = new ArrayList<>(this.allProducts);
        this.displayedProducts = new ArrayList<>();
        this.context = context;
        this.formatter = new DecimalFormat("#,###");
        this.cartManager = CartManager.getInstance(context); // Khởi tạo CartManager

        Log.d(TAG, "ProductAdapter created with " + this.allProducts.size() + " total products");
        loadInitialProducts();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.onFavoriteClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (position >= displayedProducts.size()) {
            Log.w(TAG, "Invalid position: " + position + ", displayed size: " + displayedProducts.size());
            return;
        }

        Product product = displayedProducts.get(position);

        // Set product name
        holder.tvProductName.setText(product.getProductName());

        // Set product price with formatting
        String formattedPrice = formatter.format(product.getPurchasePrice()) + " VNĐ";
        holder.tvProductPrice.setText(formattedPrice);

        // Set product condition with styling
        String rawCondition = product.getCondition();
        int idx = rawCondition.indexOf('(');
        String displayCondition;
        if (idx != -1) {
            displayCondition = rawCondition.substring(0, idx).trim();
        } else {
            displayCondition = rawCondition;
        }
        holder.tvProductCondition.setText(displayCondition);

        // Load product image using Glide
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.warnning_red_2)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(holder.ivProductImage);
        } else {
            Glide.with(context).clear(holder.ivProductImage);
            holder.ivProductImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Cập nhật icon favorite dựa trên trạng thái trong giỏ hàng
        updateFavoriteIcon(holder.ivFavorite, product.getId());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            handleFavoriteClick(product, holder.ivFavorite, position);
        });
    }

    @Override
    public int getItemCount() {
        return displayedProducts.size();
    }

    // Xử lý click vào icon favorite
    private void handleFavoriteClick(Product product, ImageView favoriteIcon, int position) {
        boolean isCurrentlyInCart = cartManager.isInCart(product.getId());

        if (isCurrentlyInCart) {
            // Xóa khỏi giỏ hàng
            boolean removed = cartManager.removeFromCart(product.getId());
            if (removed) {
                updateFavoriteIcon(favoriteIcon, product.getId());
                Toast.makeText(context, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Removed from cart: " + product.getProductName());
            } else {
                Toast.makeText(context, "Lỗi khi xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Thêm vào giỏ hàng
            boolean added = cartManager.addToCart(product);
            if (added) {
                updateFavoriteIcon(favoriteIcon, product.getId());
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Added to cart: " + product.getProductName());
            } else {
                Toast.makeText(context, "Sản phẩm đã có trong giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        }

        // Callback cho listener nếu có
        if (onFavoriteClickListener != null) {
            onFavoriteClickListener.onFavoriteClick(product, position);
        }
    }

    // Cập nhật icon favorite dựa trên trạng thái trong giỏ hàng
    private void updateFavoriteIcon(ImageView favoriteIcon, String productId) {
        boolean isInCart = cartManager.isInCart(productId);

        if (isInCart) {
            favoriteIcon.setImageResource(R.drawable.ic_heart_filled);
            favoriteIcon.setTag(true);
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_heart_outline);
            favoriteIcon.setTag(false);
        }
    }

    // Load initial products (first page)
    private void loadInitialProducts() {
        Log.d(TAG, "Loading initial products...");

        displayedProducts.clear();
        currentPage = 0;

        int endIndex = Math.min(ITEMS_PER_PAGE, filteredProducts.size());

        for (int i = 0; i < endIndex; i++) {
            displayedProducts.add(filteredProducts.get(i));
        }

        Log.d(TAG, "Initial load: " + displayedProducts.size() + " products displayed, " +
                "currentPage: " + currentPage);

        notifyDataSetChanged();
    }

    // Load more products (next page)
    public boolean loadMoreProducts() {
        Log.d(TAG, "loadMoreProducts() called");
        Log.d(TAG, "Current state - Page: " + currentPage +
                ", Displayed: " + displayedProducts.size() +
                ", Filtered: " + filteredProducts.size() +
                ", Total: " + allProducts.size());

        // Calculate indices for next batch
        int startIndex = (currentPage + 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredProducts.size());

        Log.d(TAG, "Calculating indices - startIndex: " + startIndex + ", endIndex: " + endIndex);

        // Check if there are products to load
        if (startIndex >= filteredProducts.size()) {
            Log.d(TAG, "No more products to load (startIndex >= filteredProducts.size())");
            return false;
        }

        // Prepare new products to add
        List<Product> newProducts = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            newProducts.add(filteredProducts.get(i));
        }

        if (newProducts.isEmpty()) {
            Log.d(TAG, "No new products to add");
            return false;
        }

        // Add new products to displayed list
        int insertPosition = displayedProducts.size();
        displayedProducts.addAll(newProducts);

        // Increment page counter
        currentPage++;

        Log.d(TAG, "Added " + newProducts.size() + " new products at position " + insertPosition);
        Log.d(TAG, "New state - Page: " + currentPage +
                ", Displayed: " + displayedProducts.size() +
                ", Filtered: " + filteredProducts.size() +
                ", Total: " + allProducts.size());

        // Notify adapter about new items
        notifyItemRangeInserted(insertPosition, newProducts.size());

        return true;
    }

    // Check if there are more products to load
    public boolean hasMoreProducts() {
        int nextStartIndex = (currentPage + 1) * ITEMS_PER_PAGE;
        boolean hasMore = nextStartIndex < filteredProducts.size();

        Log.d(TAG, "hasMoreProducts() - nextStartIndex: " + nextStartIndex +
                ", filteredProducts.size(): " + filteredProducts.size() +
                ", hasMore: " + hasMore);

        return hasMore;
    }

    // Get total number of products
    public int getTotalProductCount() {
        return allProducts.size();
    }

    // Get filtered product count
    public int getFilteredProductCount() {
        return filteredProducts.size();
    }

    // Get currently displayed product count
    public int getDisplayedProductCount() {
        return displayedProducts.size();
    }

    // Search products by name (tìm kiếm theo tên sản phẩm)
    public void searchProducts(String query) {
        Log.d(TAG, "searchProducts() called with query: '" + query + "'");

        currentSearchQuery = query != null ? query.toLowerCase().trim() : "";
        currentCategories.clear(); // Clear category filter khi search
        applyFilters();
    }

    // Filter products by categories (tìm kiếm theo danh mục)
    public void filterByCategories(List<String> categories) {
        Log.d(TAG, "filterByCategories() called with categories: " + categories.toString());

        currentCategories.clear();
        if (categories != null) {
            // Convert to lowercase for case-insensitive comparison
            for (String category : categories) {
                if (category != null && !category.trim().isEmpty()) {
                    currentCategories.add(category.toLowerCase().trim());
                }
            }
        }
        currentSearchQuery = ""; // Clear search query khi filter by category
        applyFilters();
    }

    // Backward compatibility - keep old method for single category
    @Deprecated
    public void filterByCategory(String category) {
        Log.d(TAG, "filterByCategory() called with category: '" + category + "'");

        List<String> categories = new ArrayList<>();
        if (category != null && !category.trim().isEmpty()) {
            categories.add(category);
        }
        filterByCategories(categories);
    }

    // Clear all filters
    public void clearFilters() {
        Log.d(TAG, "clearFilters() called");

        currentSearchQuery = "";
        currentCategories.clear();
        applyFilters();
    }

    // Apply current filters
    private void applyFilters() {
        Log.d(TAG, "Applying filters - Search: '" + currentSearchQuery + "', Categories: " + currentCategories.toString());

        filteredProducts.clear();

        for (Product product : allProducts) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            // Check search query (tìm kiếm theo tên sản phẩm)
            if (!currentSearchQuery.isEmpty()) {
                String productName = product.getProductName() != null ?
                        product.getProductName().toLowerCase() : "";
                matchesSearch = productName.contains(currentSearchQuery);
            }

            // Check categories (tìm kiếm theo danh mục)
            if (!currentCategories.isEmpty()) {
                String productCategory = product.getCategory() != null ?
                        product.getCategory().toLowerCase() : "";

                matchesCategory = false;
                // Check if product category matches any of the selected categories
                for (String category : currentCategories) {
                    if (productCategory.contains(category)) {
                        matchesCategory = true;
                        break;
                    }
                }
            }

            // Add product if it matches all criteria
            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }

        Log.d(TAG, "Filter results: " + filteredProducts.size() + " products match criteria");

        // Reset pagination and reload
        currentPage = 0;
        displayedProducts.clear();
        loadInitialProducts();
    }

    // Update all products and reset pagination
    public void updateProducts(List<Product> newProducts) {
        Log.d(TAG, "updateProducts() called with " +
                (newProducts != null ? newProducts.size() : 0) + " products");

        if (newProducts == null) {
            newProducts = new ArrayList<>();
        }

        // Update all products
        this.allProducts.clear();
        this.allProducts.addAll(newProducts);

        // Reset filters and pagination
        currentSearchQuery = "";
        currentCategories.clear();
        currentPage = 0;
        filteredProducts.clear();
        filteredProducts.addAll(allProducts);
        displayedProducts.clear();

        // Load initial products
        loadInitialProducts();

        Log.d(TAG, "Products updated - Total: " + allProducts.size() +
                ", Filtered: " + filteredProducts.size() +
                ", Displayed: " + displayedProducts.size());
    }

    // Toggle favorite state (deprecated - now using cart manager)
    @Deprecated
    private void toggleFavorite(ImageView favoriteView) {
        Object tag = favoriteView.getTag();
        boolean isFavorite = tag != null && (Boolean) tag;

        if (isFavorite) {
            favoriteView.setImageResource(R.drawable.ic_heart_outline);
            favoriteView.setTag(false);
        } else {
            favoriteView.setImageResource(R.drawable.ic_heart_filled);
            favoriteView.setTag(true);
        }
    }

    // Get product at position (useful for debugging)
    public Product getProductAt(int position) {
        if (position >= 0 && position < displayedProducts.size()) {
            return displayedProducts.get(position);
        }
        return null;
    }

    // Reset pagination (useful for refresh)
    public void resetPagination() {
        Log.d(TAG, "Resetting pagination");
        currentPage = 0;
        displayedProducts.clear();
        loadInitialProducts();
    }

    // Get current search status
    public boolean isFiltered() {
        return !currentSearchQuery.isEmpty() || !currentCategories.isEmpty();
    }

    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public List<String> getCurrentCategories() {
        return new ArrayList<>(currentCategories);
    }

    // Backward compatibility method
    @Deprecated
    public String getCurrentCategory() {
        return currentCategories.isEmpty() ? "" : currentCategories.get(0);
    }

    // Refresh favorite icons for all visible items
    public void refreshFavoriteIcons() {
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductCondition;
        ImageView ivFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductCondition = itemView.findViewById(R.id.tv_product_condition);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}