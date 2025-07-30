package com.android.projectandroid.data.userModel;

import java.io.Serializable;
import java.util.List;

/**
 * Response model cho API lấy điểm và lịch sử quyên góp của user
 */
public class UserPointsResponse implements Serializable {
    private boolean success;
    private UserPointsData data;
    private String message;

    // Constructor
    public UserPointsResponse() {}

    public UserPointsResponse(boolean success, UserPointsData data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public UserPointsData getData() { return data; }
    public void setData(UserPointsData data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /**
     * Data class chứa thông tin điểm của user
     */
    public static class UserPointsData implements Serializable {
        private String userId;
        private String username;
        private int currentPoints;
        private DonationHistory donationHistory;

        // Constructor
        public UserPointsData() {}

        public UserPointsData(String userId, String username, int currentPoints, DonationHistory donationHistory) {
            this.userId = userId;
            this.username = username;
            this.currentPoints = currentPoints;
            this.donationHistory = donationHistory;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public int getCurrentPoints() { return currentPoints; }
        public void setCurrentPoints(int currentPoints) { this.currentPoints = currentPoints; }

        public DonationHistory getDonationHistory() { return donationHistory; }
        public void setDonationHistory(DonationHistory donationHistory) { this.donationHistory = donationHistory; }

        // ✅ UTILITY METHODS
        public String getFormattedPoints() {
            if (currentPoints >= 1000000) {
                return String.format("%.1fM", currentPoints / 1000000.0);
            } else if (currentPoints >= 1000) {
                return String.format("%.1fK", currentPoints / 1000.0);
            } else {
                return String.valueOf(currentPoints);
            }
        }

        public String getPointLevel() {
            if (currentPoints >= 100000) {
                return "Diamond";
            } else if (currentPoints >= 50000) {
                return "Gold";
            } else if (currentPoints >= 20000) {
                return "Silver";
            } else if (currentPoints >= 5000) {
                return "Bronze";
            } else {
                return "Beginner";
            }
        }

        public boolean hasEnoughPoints(int requiredPoints) {
            return this.currentPoints >= requiredPoints;
        }
    }

    /**
     * Data class chứa lịch sử quyên góp
     */
    public static class DonationHistory implements Serializable {
        private int totalOrders;
        private double totalKg;
        private int totalPointsEarned;
        private List<RecentDonation> recentDonations;

        // Constructor
        public DonationHistory() {}

        public DonationHistory(int totalOrders, double totalKg, int totalPointsEarned, List<RecentDonation> recentDonations) {
            this.totalOrders = totalOrders;
            this.totalKg = totalKg;
            this.totalPointsEarned = totalPointsEarned;
            this.recentDonations = recentDonations;
        }

        // Getters and Setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public double getTotalKg() { return totalKg; }
        public void setTotalKg(double totalKg) { this.totalKg = totalKg; }

        public int getTotalPointsEarned() { return totalPointsEarned; }
        public void setTotalPointsEarned(int totalPointsEarned) { this.totalPointsEarned = totalPointsEarned; }

        public List<RecentDonation> getRecentDonations() { return recentDonations; }
        public void setRecentDonations(List<RecentDonation> recentDonations) { this.recentDonations = recentDonations; }

        // ✅ UTILITY METHODS
        public String getFormattedTotalKg() {
            return String.format("%.1f kg", totalKg);
        }

        public String getFormattedTotalPoints() {
            if (totalPointsEarned >= 1000000) {
                return String.format("%.1fM", totalPointsEarned / 1000000.0);
            } else if (totalPointsEarned >= 1000) {
                return String.format("%.1fK", totalPointsEarned / 1000.0);
            } else {
                return String.valueOf(totalPointsEarned);
            }
        }

        public double getAverageKgPerOrder() {
            return totalOrders > 0 ? totalKg / totalOrders : 0.0;
        }

        public String getFormattedAverageKg() {
            return String.format("%.1f kg/đơn", getAverageKgPerOrder());
        }

        public boolean hasRecentDonations() {
            return recentDonations != null && !recentDonations.isEmpty();
        }
    }

    /**
     * Data class cho quyên góp gần đây
     */
    public static class RecentDonation implements Serializable {
        private String kg;
        private int awardedPoints;
        private String deliveredAt;

        // Constructor
        public RecentDonation() {}

        public RecentDonation(String kg, int awardedPoints, String deliveredAt) {
            this.kg = kg;
            this.awardedPoints = awardedPoints;
            this.deliveredAt = deliveredAt;
        }

        // Getters and Setters
        public String getKg() { return kg; }
        public void setKg(String kg) { this.kg = kg; }

        public int getAwardedPoints() { return awardedPoints; }
        public void setAwardedPoints(int awardedPoints) { this.awardedPoints = awardedPoints; }

        public String getDeliveredAt() { return deliveredAt; }
        public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }

        // ✅ UTILITY METHODS
        public String getFormattedKg() {
            try {
                double kgValue = Double.parseDouble(kg);
                return String.format("%.1f kg", kgValue);
            } catch (NumberFormatException e) {
                return kg + " kg";
            }
        }

        public String getFormattedPoints() {
            if (awardedPoints >= 1000) {
                return String.format("%.1fK", awardedPoints / 1000.0);
            } else {
                return String.valueOf(awardedPoints);
            }
        }

        public String getFormattedDate() {
            if (deliveredAt == null || deliveredAt.isEmpty()) return "";

            try {
                // Parse ISO date và format lại
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");

                java.util.Date date = inputFormat.parse(deliveredAt);
                return outputFormat.format(date);
            } catch (Exception e) {
                return deliveredAt;
            }
        }

        public String getSummary() {
            return getFormattedKg() + " - " + getFormattedPoints() + " điểm";
        }
    }

    // ✅ STATIC HELPER METHODS
    public static UserPointsResponse createError(String message) {
        return new UserPointsResponse(false, null, message);
    }

    public static UserPointsResponse createSuccess(UserPointsData data) {
        return new UserPointsResponse(true, data, "Success");
    }

    @Override
    public String toString() {
        return "UserPointsResponse{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}