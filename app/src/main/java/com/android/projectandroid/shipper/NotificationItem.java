package com.android.projectandroid.shipper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    private String title;
    private String message;
    private String timestamp;
    private boolean isSuccess;
    private String orderId;
    private String address;

    public NotificationItem() {
        // Constructor rỗng cho Firebase
    }

    public NotificationItem(String title, String message, boolean isSuccess,
                            String orderId, String address, String timestamp) {
        this.title = title;
        this.message = message;
        this.isSuccess = isSuccess;
        this.orderId = orderId;
        this.address = address;
//
//        // Tạo timestamp hiện tại
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy",
//                new Locale("vi", "VN"));
//        this.timestamp = sdf.format(new Date());
        this.timestamp = timestamp ;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
