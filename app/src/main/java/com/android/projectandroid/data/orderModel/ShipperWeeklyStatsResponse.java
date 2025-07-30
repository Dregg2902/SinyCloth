package com.android.projectandroid.data.orderModel;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShipperWeeklyStatsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private WeeklyData data;

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public WeeklyData getData() { return data; }
    public void setData(WeeklyData data) { this.data = data; }

    public static class WeeklyData {
        @SerializedName("weekData")
        private List<Integer> weekData; // Mảng 7 số đại diện cho 7 ngày

        @SerializedName("weekStart")
        private String weekStart;

        @SerializedName("weekEnd")
        private String weekEnd;

        @SerializedName("weekOffset")
        private int weekOffset;

        @SerializedName("stats")
        private WeeklyStats stats;

        // Getters and setters
        public List<Integer> getWeekData() { return weekData; }
        public void setWeekData(List<Integer> weekData) { this.weekData = weekData; }

        public String getWeekStart() { return weekStart; }
        public void setWeekStart(String weekStart) { this.weekStart = weekStart; }

        public String getWeekEnd() { return weekEnd; }
        public void setWeekEnd(String weekEnd) { this.weekEnd = weekEnd; }

        public int getWeekOffset() { return weekOffset; }
        public void setWeekOffset(int weekOffset) { this.weekOffset = weekOffset; }

        public WeeklyStats getStats() { return stats; }
        public void setStats(WeeklyStats stats) { this.stats = stats; }
    }

    public static class WeeklyStats {
        @SerializedName("delivered")
        private int delivered;

        @SerializedName("cancelled")
        private int cancelled;

        @SerializedName("successRate")
        private float successRate;

        @SerializedName("failureRate")
        private float failureRate;

        @SerializedName("totalOrders")
        private int totalOrders;

        // Getters and setters
        public int getDelivered() { return delivered; }
        public void setDelivered(int delivered) { this.delivered = delivered; }

        public int getCancelled() { return cancelled; }
        public void setCancelled(int cancelled) { this.cancelled = cancelled; }

        public float getSuccessRate() { return successRate; }
        public void setSuccessRate(float successRate) { this.successRate = successRate; }

        public float getFailureRate() { return failureRate; }
        public void setFailureRate(float failureRate) { this.failureRate = failureRate; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    }
}