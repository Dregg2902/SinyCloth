package com.android.projectandroid.data.cartModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.productModel.Product;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Product> cartItems;
    private Context context;
    private OnCartItemActionListener listener;
    private DecimalFormat formatter;

    public interface OnCartItemActionListener {
        void onRemoveFromCart(Product product, int position);
        void onPurchaseProduct(Product product, int position);
        void onProductClick(Product product);
    }

    public CartAdapter(List<Product> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
        this.formatter = new DecimalFormat("#,###");
    }

    public void setOnCartItemActionListener(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartItems.get(position);

        // Reset views to default state first
        holder.itemView.setAlpha(1.0f);
        holder.tvStatus.setVisibility(View.GONE);
        holder.btnPurchase.setEnabled(true);
        holder.btnPurchase.setText("MUA NGAY");
        holder.btnPurchase.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        holder.btnPurchase.setTextColor(context.getResources().getColor(android.R.color.white));

        // Set product name
        if (product.getProductName() != null) {
            holder.tvProductName.setText(product.getProductName());
        } else {
            holder.tvProductName.setText("Sản phẩm không có tên");
        }

        // Set product price - ưu tiên sellPrice cho việc mua hàng
        double displayPrice = product.getSellPrice() > 0 ? product.getSellPrice() : product.getPurchasePrice();
        String formattedPrice = formatter.format(displayPrice) + " VNĐ";
        holder.tvProductPrice.setText(formattedPrice);

        // Set product condition
        if (product.getCondition() != null) {
            String rawCondition = product.getCondition();
            int idx = rawCondition.indexOf('(');
            String displayCondition;
            if (idx != -1) {
                displayCondition = rawCondition.substring(0, idx).trim();
            } else {
                displayCondition = rawCondition;
            }
            holder.tvProductCondition.setText(displayCondition);
        } else {
            holder.tvProductCondition.setText("Chưa xác định");
        }

        // Set product category
        if (product.getCategory() != null) {
            holder.tvProductCategory.setText(product.getCategory());
        } else {
            holder.tvProductCategory.setText("Chưa phân loại");
        }

        // Load product image
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.warnning_red_2)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Kiểm tra trạng thái sản phẩm và cập nhật UI
        updateProductStatus(holder, product);

        // Set click listeners
        setupClickListeners(holder, product, position);
    }

    /**
     * Cập nhật trạng thái hiển thị của sản phẩm
     */
    private void updateProductStatus(CartViewHolder holder, Product product) {
        String status = product.getStatus();

        if ("posting".equals(status)) {
            // Sản phẩm có thể mua được
            enablePurchaseButton(holder);
            hideStatusWarning(holder);
        } else {
            // Sản phẩm không có sẵn (hết hàng, đã bán, etc.)
            disablePurchaseButton(holder, status);
            showStatusWarning(holder, status);
        }
    }

    /**
     * Kích hoạt nút mua hàng (sản phẩm có sẵn)
     */
    private void enablePurchaseButton(CartViewHolder holder) {
        holder.btnPurchase.setEnabled(true);
        holder.btnPurchase.setText("MUA NGAY");
        holder.btnPurchase.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
        holder.btnPurchase.setTextColor(context.getResources().getColor(android.R.color.white));
    }

    /**
     * Vô hiệu hóa nút mua hàng (sản phẩm hết hàng)
     */
    private void disablePurchaseButton(CartViewHolder holder, String status) {
        holder.btnPurchase.setEnabled(false);
        holder.btnPurchase.setText("HẾT HÀNG");
        holder.btnPurchase.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        holder.btnPurchase.setTextColor(context.getResources().getColor(android.R.color.white));
    }

    /**
     * Ẩn cảnh báo trạng thái
     */
    private void hideStatusWarning(CartViewHolder holder) {
        holder.tvStatus.setVisibility(View.GONE);
    }

    /**
     * Hiển thị cảnh báo trạng thái sản phẩm
     */
    private void showStatusWarning(CartViewHolder holder, String status) {
        holder.tvStatus.setVisibility(View.VISIBLE);

        String statusText = "";
        int textColor = context.getResources().getColor(android.R.color.holo_red_dark);
        int backgroundColor = context.getResources().getColor(android.R.color.holo_red_light);

        switch (status) {
            case "sold":
                statusText = "🚫 HẾT HÀNG - Đã bán";
                break;
            case "waitingdelivery":
                statusText = "🚚 HẾT HÀNG - Đang giao hàng";
                textColor = context.getResources().getColor(android.R.color.holo_orange_dark);
                backgroundColor = context.getResources().getColor(android.R.color.holo_orange_light);
                break;
            case "delivered":
                statusText = "✅ HẾT HÀNG - Đã giao";
                textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
                backgroundColor = context.getResources().getColor(android.R.color.holo_blue_light);
                break;
            default:
                statusText = "❌ HẾT HÀNG - Không có sẵn";
                break;
        }

        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(textColor);
        holder.tvStatus.setBackgroundColor(backgroundColor);
        holder.tvStatus.setPadding(16, 8, 16, 8);
    }

    /**
     * Thiết lập các click listeners
     */
    private void setupClickListeners(CartViewHolder holder, Product product, int position) {
        String status = product.getStatus();
        boolean isAvailable = "posting".equals(status);

        // Click vào item để xem chi tiết - chỉ cho phép khi sản phẩm có sẵn
        if (isAvailable) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
            // Set visual feedback cho item có thể click
            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);
        } else {
            // Disable click cho sản phẩm hết hàng
            holder.itemView.setOnClickListener(v -> {
                String message = getUnavailableMessage(product.getStatus());
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
            // Set visual feedback cho item không thể click
            holder.itemView.setClickable(true); // Vẫn clickable để hiện toast
            holder.itemView.setFocusable(false);
            // Làm mờ item để thể hiện không khả dụng
            holder.itemView.setAlpha(0.6f);
        }

        // Nút xóa khỏi giỏ hàng - luôn hoạt động
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromCart(product, position);
            }
        });

        // Nút mua hàng - chỉ hoạt động khi sản phẩm có sẵn
        holder.btnPurchase.setOnClickListener(v -> {
            if (isAvailable) {
                if (listener != null) {
                    listener.onPurchaseProduct(product, position);
                }
            } else {
                String message = getUnavailableMessage(product.getStatus());
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Reset alpha cho sản phẩm có sẵn
        if (isAvailable) {
            holder.itemView.setAlpha(1.0f);
        }
    }

    /**
     * Lấy thông báo khi sản phẩm không có sẵn
     */
    private String getUnavailableMessage(String status) {
        switch (status) {
            case "sold":
                return "Sản phẩm này đã được bán - không thể mua";
            case "waitingdelivery":
                return "Sản phẩm này đang được giao hàng cho khách khác";
            case "delivered":
                return "Sản phẩm này đã được giao - không còn có sẵn";
            default:
                return "Sản phẩm này hiện không có sẵn để mua";
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    /**
     * Cập nhật danh sách giỏ hàng
     */
    public void updateCartItems(List<Product> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    /**
     * Xóa item khỏi adapter (deprecated - nên dùng updateCartItems)
     */
    @Deprecated
    public void removeItem(int position) {
        if (cartItems != null && position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
        }
    }

    /**
     * Cập nhật item trong adapter (deprecated - nên dùng updateCartItems)
     */
    @Deprecated
    public void updateItem(int position, Product updatedProduct) {
        if (cartItems != null && position >= 0 && position < cartItems.size()) {
            cartItems.set(position, updatedProduct);
            notifyItemChanged(position);
        }
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductCondition;
        TextView tvProductCategory;
        TextView tvStatus;
        TextView btnRemove;
        TextView btnPurchase;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_cart_product_image);
            tvProductName = itemView.findViewById(R.id.tv_cart_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_cart_product_price);
            tvProductCondition = itemView.findViewById(R.id.tv_cart_product_condition);
            tvProductCategory = itemView.findViewById(R.id.tv_cart_product_category);
            tvStatus = itemView.findViewById(R.id.tv_cart_product_status);
            btnRemove = itemView.findViewById(R.id.btn_remove_from_cart);
            btnPurchase = itemView.findViewById(R.id.btn_purchase_product);
        }
    }
}