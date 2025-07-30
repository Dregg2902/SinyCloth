package com.android.projectandroid.data.orderModel;

import java.util.List;
public class GetOrdersResponse {
    private boolean success;
    private String message;
    private List<SimpleOrder> data;
    private int count;
    private Pagination pagination;

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

    public List<SimpleOrder> getData() {
        return data;
    }

    public void setData(List<SimpleOrder> data) {
        this.data = data;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
