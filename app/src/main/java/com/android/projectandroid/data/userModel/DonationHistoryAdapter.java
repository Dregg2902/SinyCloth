package com.android.projectandroid.data.userModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.SimpleOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị lịch sử quyên góp
 */
public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {

    private List<SimpleOrder> orders;
    private OnItemClickListener onItemClickListener;

    // Interface cho click events
    public interface OnItemClickListener {
        void onItemClick(SimpleOrder order, int position);
        void onViewDetailsClick(SimpleOrder order, int position);
        void onShareClick(SimpleOrder order, int position);
    }

    // Constructor
    public DonationHistoryAdapter(List<SimpleOrder> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    // Setter cho click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // Update data method
    public void updateData(List<SimpleOrder> newOrders) {
        this.orders.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    // Add single order
    public void addOrder(SimpleOrder order) {
        if (order != null) {
            this.orders.add(0, order); // Add to beginning
            notifyItemInserted(0);
        }
    }

    // Remove order
    public void removeOrder(int position) {
        if (position >= 0 && position < orders.size()) {
            orders.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Update single order
    public void updateOrder(int position, SimpleOrder updatedOrder) {
        if (position >= 0 && position < orders.size() && updatedOrder != null) {
            orders.set(position, updatedOrder);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SimpleOrder order = orders.get(position);
        holder.bind(order, position);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvKg;
        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvPoints;
        private TextView tvWishMessage;
        private View viewStatusIndicator;
        private View layoutPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }

        private void initViews() {
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvKg = itemView.findViewById(R.id.tvKg);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvWishMessage = itemView.findViewById(R.id.tvWishMessage);
            layoutPoints = itemView.findViewById(R.id.layoutPoints);
        }

        private void setupClickListeners() {
            // Main item click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(orders.get(position), position);
                }
            });

            // Future: Add more click listeners for buttons
            /*
            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onViewDetailsClick(orders.get(position), position);
                }
            });
            */
        }

        public void bind(SimpleOrder order, int position) {
            // ✅ PRODUCT NAME
            String productName = order.getProductName();
            tvProductName.setText(productName.isEmpty() ? "Sản phẩm quyên góp" : productName);

            // ✅ KG INFO
            String kgInfo = order.getFormattedKgInfo();
            tvKg.setText(kgInfo.isEmpty() ? "0 kg" : kgInfo);

            // ✅ STATUS
            tvStatus.setText(order.getStatusDisplayText());
            tvStatus.setTextColor(order.getStatusColor());
            viewStatusIndicator.setBackgroundColor(order.getStatusColor());

            // ✅ STATUS BACKGROUND
            updateStatusBackground(order.getStatus());

            // ✅ DATE
            tvDate.setText(order.getFormattedOrderDate());

            // ✅ POINTS
            updatePointsDisplay(order);

            // ✅ WISH MESSAGE
            updateWishMessage(order);
        }

        private void updateStatusBackground(String status) {
            int backgroundRes;
            switch (status != null ? status.toLowerCase() : "") {
                case "delivered":
                    backgroundRes = R.drawable.bg_status_delivered;
                    break;
                case "cancelled":
                    backgroundRes = R.drawable.bg_status_cancelled;
                    break;
                case "shipping":
                default:
                    backgroundRes = R.drawable.bg_status_shipping;
                    break;
            }
            tvStatus.setBackgroundResource(backgroundRes);
        }

        private void updatePointsDisplay(SimpleOrder order) {
            if (order.isDonationOrder()) {
                layoutPoints.setVisibility(View.VISIBLE);

                if (order.isDelivered()) {
                    // Đã hoàn thành - hiển thị điểm đã nhận
                    int points = order.calculateRewardPoints();
                    tvPoints.setText("+" + points);
                    tvPoints.setTextColor(itemView.getContext().getResources().getColor(R.color.success_color));
                } else if (order.isShipping()) {
                    // Đang giao - hiển thị điểm dự kiến
                    int expectedPoints = order.calculateRewardPoints();
                    tvPoints.setText("~" + expectedPoints);
                    tvPoints.setTextColor(itemView.getContext().getResources().getColor(R.color.warning_color));
                } else {
                    // Đã hủy - ẩn điểm
                    layoutPoints.setVisibility(View.GONE);
                }
            } else {
                layoutPoints.setVisibility(View.GONE);
            }
        }

        private void updateWishMessage(SimpleOrder order) {
            String notes = order.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                tvWishMessage.setVisibility(View.VISIBLE);
                tvWishMessage.setText("💌 " + notes);
            } else {
                tvWishMessage.setVisibility(View.GONE);
            }
        }
    }

    // ✅ UTILITY METHODS

    /**
     * Filter orders by status
     */
    public void filterByStatus(String status) {
        // Implementation for filtering (if needed)
        // This could be enhanced to filter the current list
    }

    /**
     * Sort orders by date
     */
    public void sortByDate(boolean ascending) {
        if (orders != null && !orders.isEmpty()) {
            orders.sort((o1, o2) -> {
                // Implement date sorting based on orderDate
                if (ascending) {
                    return o1.getOrderDate().compareTo(o2.getOrderDate());
                } else {
                    return o2.getOrderDate().compareTo(o1.getOrderDate());
                }
            });
            notifyDataSetChanged();
        }
    }

    /**
     * Get total kg from current orders
     */
    public double getTotalKg() {
        double total = 0.0;
        for (SimpleOrder order : orders) {
            if (order.isDonationOrder() && order.getKg() != null) {
                try {
                    double kg = Double.parseDouble(order.getKg());
                    total += kg;
                } catch (NumberFormatException e) {
                    // Ignore invalid kg values
                }
            }
        }
        return total;
    }

    /**
     * Get total points from current orders
     */
    public int getTotalPoints() {
        int total = 0;
        for (SimpleOrder order : orders) {
            if (order.isDonationOrder() && order.isDelivered()) {
                total += order.calculateRewardPoints();
            }
        }
        return total;
    }

    /**
     * Get count by status
     */
    public int getCountByStatus(String status) {
        int count = 0;
        for (SimpleOrder order : orders) {
            if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if list is empty
     */
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * Get order at position
     */
    public SimpleOrder getOrderAt(int position) {
        if (position >= 0 && position < orders.size()) {
            return orders.get(position);
        }
        return null;
    }

    /**
     * Find order by ID
     */
    public int findOrderPositionById(String orderId) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId() != null && orders.get(i).getId().equals(orderId)) {
                return i;
            }
        }
        return -1;
    }
}