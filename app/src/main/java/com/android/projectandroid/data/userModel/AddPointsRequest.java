package com.android.projectandroid.data.userModel;

import java.io.Serializable;
public class AddPointsRequest implements Serializable {
    private String type;
    private String userId;
    private int points;

    // Constructor
    public AddPointsRequest() {}

    // Getters and Setters
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getType() {
        return type;
    }

    public void setType() {
        this.type = "update_points";
    }

    // ✅ VALIDATION METHODS
    public boolean isValid() {
        return points > 0;
    }

    public String getValidationError() {
        if (points <= 0) {
            return "Điểm phải lớn hơn 0";
        }
        return null;
    }
}