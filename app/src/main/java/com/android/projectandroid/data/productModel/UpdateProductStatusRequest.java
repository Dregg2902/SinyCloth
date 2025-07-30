package com.android.projectandroid.data.productModel;

public class UpdateProductStatusRequest {
    private String status;
    private String rejectionReason;

    public UpdateProductStatusRequest() {}

    public UpdateProductStatusRequest(String status) {
        this.status = status;
    }

    public UpdateProductStatusRequest(String status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}