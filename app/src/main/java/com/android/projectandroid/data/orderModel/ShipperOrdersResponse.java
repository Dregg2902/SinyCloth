package com.android.projectandroid.data.orderModel;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShipperOrdersResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<SimpleOrder> data;

    @SerializedName("stats")
    private ShipperStats stats;

    @SerializedName("pagination")
    private Pagination pagination;

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<SimpleOrder> getData() { return data; }
    public void setData(List<SimpleOrder> data) { this.data = data; }

    public ShipperStats getStats() { return stats; }
    public void setStats(ShipperStats stats) { this.stats = stats; }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    // Inner classes
    public static class ShipperStats {
        @SerializedName("delivered")
        private StatItem delivered;

        @SerializedName("cancelled")
        private StatItem cancelled;

        @SerializedName("successRate")
        private float successRate;

        @SerializedName("failureRate")
        private float failureRate;

        @SerializedName("totalOrders")
        private int totalOrders;

        @SerializedName("completedOrders")
        private int completedOrders;

        // Getters and setters
        public StatItem getDelivered() { return delivered; }
        public void setDelivered(StatItem delivered) { this.delivered = delivered; }

        public StatItem getCancelled() { return cancelled; }
        public void setCancelled(StatItem cancelled) { this.cancelled = cancelled; }

        public float getSuccessRate() { return successRate; }
        public void setSuccessRate(float successRate) { this.successRate = successRate; }

        public float getFailureRate() { return failureRate; }
        public void setFailureRate(float failureRate) { this.failureRate = failureRate; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    }

    public static class StatItem {
        @SerializedName("count")
        private int count;

        @SerializedName("totalValue")
        private double totalValue;

        // Getters and setters
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public double getTotalValue() { return totalValue; }
        public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    }

    public static class Pagination {
        @SerializedName("currentPage")
        private int currentPage;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("totalOrders")
        private int totalOrders;

        @SerializedName("hasNextPage")
        private boolean hasNextPage;

        @SerializedName("hasPrevPage")
        private boolean hasPrevPage;

        // Getters and setters
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public boolean isHasNextPage() { return hasNextPage; }
        public void setHasNextPage(boolean hasNextPage) { this.hasNextPage = hasNextPage; }

        public boolean isHasPrevPage() { return hasPrevPage; }
        public void setHasPrevPage(boolean hasPrevPage) { this.hasPrevPage = hasPrevPage; }
    }
}
