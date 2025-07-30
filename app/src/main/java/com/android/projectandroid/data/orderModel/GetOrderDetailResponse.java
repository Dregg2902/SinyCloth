package com.android.projectandroid.data.orderModel;

public class GetOrderDetailResponse {
    private boolean success;
    private String message;
    private SimpleOrder data;

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

    public SimpleOrder getData() {
        return data;
    }

    public void setData(SimpleOrder data) {
        this.data = data;
    }
}
