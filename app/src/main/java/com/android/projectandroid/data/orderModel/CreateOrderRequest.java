package com.android.projectandroid.data.orderModel;

public class CreateOrderRequest {
    private String userId;
    private String productId;
    private String orderType;
    private String notes;
    private String kg;
    private Double price;

    // Constructor gốc (không có giá)
    public CreateOrderRequest(String userId, String productId, String orderType, String notes, String kg) {
        this.userId = userId;
        this.productId = productId;
        this.orderType = orderType;
        this.notes = notes;
        this.kg = kg;
        this.price = 0.0 ;
    }

    // Constructor với giá
    public CreateOrderRequest(String userId, String productId, String orderType, String notes, String kg, Double price) {
        this.userId = userId;
        this.productId = productId;
        this.orderType = orderType;
        this.notes = notes;
        this.kg = kg;
        this.price = price;
    }

    // Getter methods
    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getNotes() {
        return notes;
    }

    public String getKg() {
        return kg;
    }

    public double getPrice() {
        return price;
    }

    // Setter methods
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setKg(String kg) {
        this.kg = kg;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", orderType='" + orderType + '\'' +
                ", notes='" + notes + '\'' +
                ", kg='" + kg + '\'' +
                ", price=" + price +
                '}';
    }
}