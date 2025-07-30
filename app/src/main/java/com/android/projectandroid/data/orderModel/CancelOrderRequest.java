package com.android.projectandroid.data.orderModel;

public class CancelOrderRequest {
    private String cancelReason;
    private String userId; // Optional để verify ownership

    private String shipperId;

    // Constructors
    public CancelOrderRequest() {
    }

    public CancelOrderRequest(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public CancelOrderRequest(String cancelReason, String userId) {
        this.cancelReason = cancelReason;
        this.userId = userId;
    }

    // Getters and Setters
    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getShipperId() {
        return shipperId;
    }

    public void setShipperId(String shipperId) {
        this.shipperId = shipperId;
    }
}