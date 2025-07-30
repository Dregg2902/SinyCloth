package com.android.projectandroid.data.productModel;

import java.util.List;

public class GetProductsResponse {
    private boolean success;
    private String message;
    private List<Product> data;
    private int count;

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Product> getData() { return data; }
    public void setData(List<Product> data) { this.data = data; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
