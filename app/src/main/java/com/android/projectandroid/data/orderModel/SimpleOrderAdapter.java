package com.android.projectandroid.data.orderModel;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class SimpleOrderAdapter extends RecyclerView.Adapter<SimpleOrderAdapter.OrderViewHolder> {

    private static final String TAG = "SimpleOrderAdapter";
    private List<SimpleOrder> orderList;
    private Context context;
    private OnOrderActionListener actionListener;

    // Interface for callbacks
    public interface OnOrderActionListener {
        void onViewOrderDetail(SimpleOrder order);
        void onCancelOrder(SimpleOrder order);
        void onContactSeller(SimpleOrder order);
        void onReorderProduct(SimpleOrder order);
    }

    public SimpleOrderAdapter(List<SimpleOrder> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_simple_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (position >= orderList.size()) {
            Log.w(TAG, "Invalid position: " + position);
            return;
        }

        SimpleOrder order = orderList.get(position);
        bindOrderData(holder, order);
        setupClickListeners(holder, order);
    }

    private void bindOrderData(OrderViewHolder holder, SimpleOrder order) {
        // Order ID (truncated)
        String orderId = order.getId();
        if (orderId != null && orderId.length() > 8) {
            orderId = "#..." + orderId.substring(orderId.length() - 8);
        } else {
            orderId = "#" + orderId;
        }
        holder.tvOrderId.setText(orderId);

        // Product name and price
        holder.tvProductName.setText(order.getProductName());
        holder.tvProductPrice.setText(order.getFormattedPrice());

        // Order date
        holder.tvOrderDate.setText(order.getFormattedOrderDate());

        // Customer info
        holder.tvCustomerName.setText(order.getCustomerName());
        holder.tvCustomerPhone.setText(order.getCustomerPhone());
        holder.tvCustomerAddress.setText(order.getCustomerAddress());

        // Notes
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            holder.tvNotes.setText("📝 " + order.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // ✅ SET STATUS WITH ICON VÀ COLOR CHO 3 TRẠNG THÁI
        setStatusDisplay(holder.tvOrderStatus, order.getStatus(), order.getStatusIcon());

        // Set order type with color and icon
        setOrderTypeDisplay(holder.tvOrderType, order.getOrderType());

        // Load product image
        loadProductImage(holder.ivProductImage, order);

        // ✅ UPDATE ACTION BUTTONS CHO 3 TRẠNG THÁI
        updateActionButtons(holder, order);

        // ✅ HIỂN THỊ THÔNG TIN CHI TIẾT TRẠNG THÁI
        displayStatusDetails(holder, order);
    }

    // ✅ SET STATUS DISPLAY CHO 3 TRẠNG THÁI
    private void setStatusDisplay(TextView statusView, String status, String icon) {
        String displayText = "";
        int colorRes = 0xFF757575;

        switch (status.toLowerCase()) {
            case "shipping":
                displayText = "Đang giao";
                colorRes = 0xFF2196F3; // Blue
                break;
            case "delivered":
                displayText = "Đã giao";
                colorRes = 0xFF4CAF50; // Green
                break;
            case "cancelled":
                displayText = "Đã hủy";
                colorRes = 0xFFEF5350; // Red
                break;
            default:
                displayText = "Không xác định";
                colorRes = 0xFF757575; // Grey
        }

        statusView.setText(icon + " " + displayText);

        // Create rounded background with color
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(colorRes);
        background.setCornerRadius(20f);
        statusView.setBackground(background);
    }

    private void setOrderTypeDisplay(TextView orderTypeView, String orderType) {
        String displayText = "";
        String icon = "";
        int colorRes = 0xFF757575;

        switch (orderType.toLowerCase()) {
            case "pass_pickup":
                displayText = "Đi lấy đồ pass";
                icon = "🎯 ";
                colorRes = 0xFF2196F3; // Blue
                break;
            case "donation_pickup":
                displayText = "Đi lấy đồ quyên góp";
                icon = "💝 ";
                colorRes = 0xFF4CAF50; // Green
                break;
            case "delivery":
                displayText = "Đi giao hàng";
                icon = "🚚 ";
                colorRes = 0xFFFF9800; // Orange
                break;
            default:
                displayText = "Không xác định";
                icon = "❓ ";
                colorRes = 0xFF757575; // Grey
        }

        orderTypeView.setText(icon + displayText);

        // Create rounded background with color
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(colorRes);
        background.setCornerRadius(20f);
        orderTypeView.setBackground(background);
    }

    // ✅ HIỂN THỊ CHI TIẾT TRẠNG THÁI
    private void displayStatusDetails(OrderViewHolder holder, SimpleOrder order) {
        String statusDetail = "";

        switch (order.getStatus().toLowerCase()) {
            case "shipping":
                statusDetail = "⏰ Đơn hàng đang được giao đến bạn";
                break;
            case "delivered":
                String deliveredTime = order.getFormattedDeliveredTime();
                statusDetail = "✅ Đã giao thành công";
                if (!deliveredTime.isEmpty()) {
                    statusDetail += " lúc " + deliveredTime;
                }
                break;
            case "cancelled":
                String cancelledTime = order.getFormattedCancelledTime();
                statusDetail = "❌ Đã hủy";
                if (!cancelledTime.isEmpty()) {
                    statusDetail += " lúc " + cancelledTime;
                }
                if (order.getCancelReason() != null) {
                    statusDetail += "\nLý do: " + order.getCancelReason();
                }
                break;
        }

        if (!statusDetail.isEmpty()) {
            holder.tvStatusDetails.setText(statusDetail);
            holder.tvStatusDetails.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusDetails.setVisibility(View.GONE);
        }
    }

    private void loadProductImage(ImageView imageView, SimpleOrder order) {
        String imageUrl = order.getProductImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.warnning_red_2)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(imageView);
        } else {
            Glide.with(context).clear(imageView);
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    // ✅ UPDATE ACTION BUTTONS CHO 3 TRẠNG THÁI
    private void updateActionButtons(OrderViewHolder holder, SimpleOrder order) {
        // Always show view details button
        holder.btnViewDetails.setVisibility(View.VISIBLE);

        // ✅ LOGIC BUTTONS CHO 3 TRẠNG THÁI
        switch (order.getStatus().toLowerCase()) {
            case "shipping":
                // Đang giao: có thể hủy và liên hệ
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setText("Hủy đơn");
                holder.btnCancel.setEnabled(true);

                holder.btnContact.setVisibility(View.VISIBLE);
                holder.btnContact.setText("Liên hệ");

                holder.btnReorder.setVisibility(View.GONE);
                break;

            case "delivered":
                // Đã giao: có thể liên hệ và đặt lại
                holder.btnCancel.setVisibility(View.GONE);

                holder.btnContact.setVisibility(View.VISIBLE);
                holder.btnContact.setText("Đánh giá");

                holder.btnReorder.setVisibility(View.VISIBLE);
                holder.btnReorder.setText("Mua lại");
                break;

            case "cancelled":
                // Đã hủy: chỉ có thể đặt lại
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnContact.setVisibility(View.GONE);

                holder.btnReorder.setVisibility(View.VISIBLE);
                holder.btnReorder.setText("Đặt lại");
                break;

            default:
                // Hide all action buttons for unknown status
                holder.btnCancel.setVisibility(View.GONE);
                holder.btnContact.setVisibility(View.GONE);
                holder.btnReorder.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners(OrderViewHolder holder, SimpleOrder order) {
        // Item click - view details
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewOrderDetail(order);
            }
        });

        // View details button
        holder.btnViewDetails.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onViewOrderDetail(order);
            }
        });

        // Cancel button
        holder.btnCancel.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onCancelOrder(order);
            }
        });

        // Contact button
        holder.btnContact.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onContactSeller(order);
            }
        });

        // Reorder button
        holder.btnReorder.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onReorderProduct(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<SimpleOrder> newOrders) {
        this.orderList.clear();
        if (newOrders != null) {
            this.orderList.addAll(newOrders);
        }
        notifyDataSetChanged();
        Log.d(TAG, "Orders updated, total count: " + orderList.size());
    }

    public void addOrder(SimpleOrder order) {
        if (order != null) {
            orderList.add(0, order); // Add to beginning
            notifyItemInserted(0);
            Log.d(TAG, "Order added: " + order.getId());
        }
    }

    public void updateOrder(SimpleOrder updatedOrder) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId().equals(updatedOrder.getId())) {
                orderList.set(i, updatedOrder);
                notifyItemChanged(i);
                Log.d(TAG, "Order updated: " + updatedOrder.getId());
                break;
            }
        }
    }

    public void removeOrder(String orderId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId().equals(orderId)) {
                orderList.remove(i);
                notifyItemRemoved(i);
                Log.d(TAG, "Order removed: " + orderId);
                break;
            }
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvProductName, tvProductPrice;
        TextView tvOrderType, tvCustomerName, tvCustomerPhone, tvCustomerAddress;
        TextView tvOrderDate, tvNotes, tvStatusDetails;
        ImageView ivProductImage;
        Button btnViewDetails, btnCancel, btnContact, btnReorder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all views
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvOrderType = itemView.findViewById(R.id.tv_order_type);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvCustomerAddress = itemView.findViewById(R.id.tv_customer_address);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvNotes = itemView.findViewById(R.id.tv_notes);
            tvStatusDetails = itemView.findViewById(R.id.tv_status_details); // ✅ THÊM VIEW MỚI

            ivProductImage = itemView.findViewById(R.id.iv_product_image);

            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnContact = itemView.findViewById(R.id.btn_contact);
            btnReorder = itemView.findViewById(R.id.btn_reorder);
        }
    }
}