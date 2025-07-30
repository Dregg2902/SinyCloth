// NormalUser.java - Cập nhật model thêm fullName và avatarUrl
package com.android.projectandroid.data.userModel;

import java.io.Serializable;

public class NormalUser implements Serializable {

    private String _id;
    private String username;
    private String password;
    private String email;
    private String fullName;    // ✅ THÊM TRƯỜNG TÊN ĐẦY ĐỦ
    private String dateOfBirth;
    private String phoneNumber;
    private String address;
    private String gender;
    private int points;
    private String avatarUrl;   // ✅ THÊM TRƯỜNG AVATAR URL

    // ✅ GETTERS VÀ SETTERS CHO FULLNAME
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    // ✅ GETTERS VÀ SETTERS CHO AVATAR URL
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // ✅ LẤY TÊN HIỂN THỊ (ưu tiên fullName, nếu không có thì dùng username)
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username != null ? username : "User";
    }

    // ✅ KIỂM TRA CÓ AVATAR HAY KHÔNG
    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.trim().isEmpty();
    }

    // GETTERS VÀ SETTERS CHO POINTS
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    // Getters và Setters cho address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Getters và Setters cho gender
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    // UTILITY METHODS CHO ĐIỂM
    public String getFormattedPoints() {
        if (points >= 1000000) {
            return String.format("%.1fM", points / 1000000.0);
        } else if (points >= 1000) {
            return String.format("%.1fK", points / 1000.0);
        } else {
            return String.valueOf(points);
        }
    }

    public boolean hasEnoughPoints(int requiredPoints) {
        return this.points >= requiredPoints;
    }

    public int calculateRedeemableRewards(int pointsPerReward) {
        if (pointsPerReward <= 0) return 0;
        return this.points / pointsPerReward;
    }

    public double calculateKgNeededForPoints(int targetPoints) {
        if (targetPoints <= this.points) return 0.0;
        int pointsNeeded = targetPoints - this.points;
        return Math.ceil(pointsNeeded / 1000.0); // 1000 điểm/kg
    }

    public String getPointLevel() {
        if (points >= 100000) {
            return "Diamond";
        } else if (points >= 50000) {
            return "Gold";
        } else if (points >= 20000) {
            return "Silver";
        } else if (points >= 5000) {
            return "Bronze";
        } else {
            return "Beginner";
        }
    }

    public int getPointLevelColor() {
        String level = getPointLevel();
        switch (level) {
            case "Diamond":
                return 0xFF00BCD4; // Cyan
            case "Gold":
                return 0xFFFFD700; // Gold
            case "Silver":
                return 0xFFC0C0C0; // Silver
            case "Bronze":
                return 0xFFCD7F32; // Bronze
            default:
                return 0xFF757575; // Grey
        }
    }

    public int getProgressToNextLevel() {
        String currentLevel = getPointLevel();
        int currentPoints = this.points;

        switch (currentLevel) {
            case "Beginner":
                return Math.min(100, (currentPoints * 100) / 5000);
            case "Bronze":
                return Math.min(100, ((currentPoints - 5000) * 100) / 15000);
            case "Silver":
                return Math.min(100, ((currentPoints - 20000) * 100) / 30000);
            case "Gold":
                return Math.min(100, ((currentPoints - 50000) * 100) / 50000);
            case "Diamond":
                return 100; // Max level
            default:
                return 0;
        }
    }

    public int getPointsNeededForNextLevel() {
        String currentLevel = getPointLevel();
        int currentPoints = this.points;

        switch (currentLevel) {
            case "Beginner":
                return 5000 - currentPoints;
            case "Bronze":
                return 20000 - currentPoints;
            case "Silver":
                return 50000 - currentPoints;
            case "Gold":
                return 100000 - currentPoints;
            case "Diamond":
                return 0; // Max level
            default:
                return 5000;
        }
    }

    // ✅ KIỂM TRA HỒ SƠ HOÀN THIỆN
    public boolean isProfileComplete() {
        return username != null && !username.isEmpty() &&
                email != null && !email.isEmpty() &&
                fullName != null && !fullName.isEmpty() &&
                phoneNumber != null && !phoneNumber.isEmpty() &&
                address != null && !address.isEmpty();
    }

    public double getProfileCompleteness() {
        int totalFields = 7; // username, email, fullName, phone, address, gender, dateOfBirth
        int completedFields = 0;

        if (username != null && !username.isEmpty()) completedFields++;
        if (email != null && !email.isEmpty()) completedFields++;
        if (fullName != null && !fullName.isEmpty()) completedFields++;
        if (phoneNumber != null && !phoneNumber.isEmpty()) completedFields++;
        if (address != null && !address.isEmpty()) completedFields++;
        if (gender != null && !gender.isEmpty()) completedFields++;
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) completedFields++;

        return (completedFields * 100.0) / totalFields;
    }

    @Override
    public String toString() {
        return "NormalUser{" +
                "_id='" + _id + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", points=" + points +
                ", level='" + getPointLevel() + '\'' +
                ", hasAvatar=" + hasAvatar() +
                '}';
    }
}