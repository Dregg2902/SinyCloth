package com.android.projectandroid.data.userModel;

import java.io.Serializable;

public class AddPointsResponse implements Serializable {
    private boolean success;
    private String message;
    private PointsData data;

    // Constructor
    public AddPointsResponse() {}

    public AddPointsResponse(boolean success, String message, PointsData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PointsData getData() {
        return data;
    }

    public void setData(PointsData data) {
        this.data = data;
    }

    // ✅ UTILITY METHODS
    public boolean hasData() {
        return data != null;
    }

    public int getPointsAdded() {
        return hasData() ? data.getAddedPoints() : 0;
    }

    public int getCurrentPoints() {
        return hasData() ? data.getCurrentPoints() : 0;
    }

    // ✅ STATIC FACTORY METHODS
    public static AddPointsResponse createSuccess(PointsData data) {
        return new AddPointsResponse(true, "Cộng điểm thành công", data);
    }

    public static AddPointsResponse createError(String message) {
        return new AddPointsResponse(false, message, null);
    }

    @Override
    public String toString() {
        return "AddPointsResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * Nested class chứa dữ liệu chi tiết về việc cộng điểm
     */
    public static class PointsData implements Serializable {
        private String userId;
        private String username;
        private int previousPoints;
        private int addedPoints;
        private int currentPoints;
        private String reason;

        // Constructor
        public PointsData() {}

        public PointsData(String userId, String username, int previousPoints,
                          int addedPoints, int currentPoints, String reason) {
            this.userId = userId;
            this.username = username;
            this.previousPoints = previousPoints;
            this.addedPoints = addedPoints;
            this.currentPoints = currentPoints;
            this.reason = reason;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getPreviousPoints() {
            return previousPoints;
        }

        public void setPreviousPoints(int previousPoints) {
            this.previousPoints = previousPoints;
        }

        public int getAddedPoints() {
            return addedPoints;
        }

        public void setAddedPoints(int addedPoints) {
            this.addedPoints = addedPoints;
        }

        public int getCurrentPoints() {
            return currentPoints;
        }

        public void setCurrentPoints(int currentPoints) {
            this.currentPoints = currentPoints;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        // ✅ UTILITY METHODS
        public String getFormattedPreviousPoints() {
            return PointsUtils.formatPoints(previousPoints);
        }

        public String getFormattedAddedPoints() {
            return "+" + PointsUtils.formatPoints(addedPoints);
        }

        public String getFormattedCurrentPoints() {
            return PointsUtils.formatPoints(currentPoints);
        }

        public String getPreviousLevel() {
            return PointsUtils.getLevelFromPoints(previousPoints);
        }

        public String getCurrentLevel() {
            return PointsUtils.getLevelFromPoints(currentPoints);
        }

        public boolean isLevelUp() {
            return PointsUtils.isLevelUp(previousPoints, currentPoints);
        }

        public String getLevelUpMessage() {
            if (isLevelUp()) {
                return PointsUtils.getLevelUpMessage(previousPoints, currentPoints);
            }
            return "";
        }

        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("Đã cộng ").append(getFormattedAddedPoints()).append(" điểm");

            if (reason != null && !reason.isEmpty()) {
                summary.append(" (").append(reason).append(")");
            }

            if (isLevelUp()) {
                summary.append("\n").append(getLevelUpMessage());
            }

            return summary.toString();
        }

        @Override
        public String toString() {
            return "PointsData{" +
                    "userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    ", previousPoints=" + previousPoints +
                    ", addedPoints=" + addedPoints +
                    ", currentPoints=" + currentPoints +
                    ", reason='" + reason + '\'' +
                    ", levelUp=" + isLevelUp() +
                    '}';
        }
    }
}