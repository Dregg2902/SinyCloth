package com.android.projectandroid.data.orderModel;

public class UpdateOrderStatusRequest {
    private String status;
    private String notes;
    private String shipperId;

    // Constructors
    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(String status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getShipperId() {
        return shipperId;
    }

    public void setShipperId(String shipperId) {
        this.shipperId = shipperId;
    }
}
