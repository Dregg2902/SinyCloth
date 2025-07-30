package com.android.projectandroid.data.orderModel;

public class UpdateOrderResponse {
    private boolean success;
    private String message;
    private OrderData data;

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

    public OrderData getData() {
        return data;
    }

    public void setData(OrderData data) {
        this.data = data;
    }
    public static class OrderData extends SimpleOrder {
        private String pointsMessage; // ✅ THÔNG BÁO ĐIỂM ĐÃ THƯỞNG

        public String getPointsMessage() { return pointsMessage; }
    }
}
