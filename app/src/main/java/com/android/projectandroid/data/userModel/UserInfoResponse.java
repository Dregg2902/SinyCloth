package com.android.projectandroid.data.userModel;

import java.io.Serializable;

/**
 * Response model cho thông tin user (bao gồm điểm)
 */
public class UserInfoResponse implements Serializable {
    private boolean success;
    private String error;
    private NormalUser user;

    // Constructor
    public UserInfoResponse() {}

    public UserInfoResponse(boolean success, String error, NormalUser user) {
        this.success = success;
        this.error = error;
        this.user = user;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public NormalUser getUser() {
        return user;
    }

    public void setUser(NormalUser user) {
        this.user = user;
    }

    // ✅ UTILITY METHODS
    public boolean hasUser() {
        return user != null;
    }

    public int getUserPoints() {
        return hasUser() ? user.getPoints() : 0;
    }

    public String getUserLevel() {
        return hasUser() ? user.getPointLevel() : "Unknown";
    }

    // ✅ STATIC FACTORY METHODS
    public static UserInfoResponse createSuccess(NormalUser user) {
        return new UserInfoResponse(true, null, user);
    }

    public static UserInfoResponse createError(String error) {
        return new UserInfoResponse(false, error, null);
    }

    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", user=" + user +
                '}';
    }

    /**
     * Nested class chứa thông tin chi tiết của user
     */
    public static class UserData implements Serializable {
        private String _id;
        private String username;
        private String fullname;
        private String email;
        private String dateOfBirth;
        private String phoneNumber;
        private String address;
        private String gender;
        private int points;
        private String type;
        private String avatar;

        // Constructor
        public UserData() {}

        public UserData(String _id, String username,String fullname, String email, String dateOfBirth,
                        String phoneNumber, String address, String gender, int points, String type,String avatar) {
            this._id = _id;
            this.username = username;
            this.fullname = fullname;
            this.email = email;
            this.dateOfBirth = dateOfBirth;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.gender = gender;
            this.points = points;
            this.type = type;
            this.avatar = avatar;
        }

        // Getters and Setters
        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        // ✅ UTILITY METHODS FOR POINTS
        public String getFormattedPoints() {
            return PointsUtils.formatPoints(points);
        }

        public String getPointLevel() {
            return PointsUtils.getLevelFromPoints(points);
        }

        public int getPointLevelColor() {
            return PointsUtils.getColorForLevel(getPointLevel());
        }

        public boolean hasEnoughPoints(int requiredPoints) {
            return this.points >= requiredPoints;
        }

        public double calculateKgNeededForPoints(int targetPoints) {
            if (targetPoints <= this.points) return 0.0;
            int pointsNeeded = targetPoints - this.points;
            return PointsUtils.calculateKgForPoints(pointsNeeded);
        }

        public int getProgressToNextLevel() {
            return PointsUtils.getProgressToNextLevel(points);
        }

        public int getPointsNeededForNextLevel() {
            return PointsUtils.getPointsNeededForNextLevel(points);
        }

        public String getNextLevelInfo() {
            int pointsNeeded = getPointsNeededForNextLevel();
            if (pointsNeeded <= 0) {
                return "Đã đạt level cao nhất!";
            }

            double kgNeeded = PointsUtils.calculateKgForPoints(pointsNeeded);
            return String.format("Cần %s điểm (%.1f kg) để lên cấp",
                    PointsUtils.formatPoints(pointsNeeded), kgNeeded);
        }

        // ✅ PROFILE COMPLETENESS
        public boolean isProfileComplete() {
            return username != null && !username.isEmpty() &&
                    email != null && !email.isEmpty() &&
                    phoneNumber != null && !phoneNumber.isEmpty() &&
                    address != null && !address.isEmpty();
        }

        public double getProfileCompleteness() {
            int totalFields = 6; // username, email, phone, address, gender, dateOfBirth
            int completedFields = 0;

            if (username != null && !username.isEmpty()) completedFields++;
            if (email != null && !email.isEmpty()) completedFields++;
            if (phoneNumber != null && !phoneNumber.isEmpty()) completedFields++;
            if (address != null && !address.isEmpty()) completedFields++;
            if (gender != null && !gender.isEmpty()) completedFields++;
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) completedFields++;
            if (avatar != null && !avatar.isEmpty()) completedFields++;

            return (completedFields * 100.0) / totalFields;
        }

        // ✅ DISPLAY METHODS
        public String getDisplayName() {
            return username != null ? username : "User";
        }

        public String getShortInfo() {
            return getDisplayName() + " - " + getFormattedPoints() + " điểm (" + getPointLevel() + ")";
        }

        public String getProfileSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("👤 ").append(getDisplayName()).append("\n");
            summary.append("🏆 ").append(getFormattedPoints()).append(" điểm - ").append(getPointLevel()).append("\n");

            if (getPointsNeededForNextLevel() > 0) {
                summary.append("🎯 ").append(getNextLevelInfo()).append("\n");
            }

            summary.append("📊 Hồ sơ hoàn thiện ").append(String.format("%.0f%%", getProfileCompleteness()));

            return summary.toString();
        }

        @Override
        public String toString() {
            return "UserData{" +
                    "_id='" + _id + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", points=" + points +
                    ", level='" + getPointLevel() + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}