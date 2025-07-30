package com.android.projectandroid.data.productModel;

import com.android.projectandroid.data.userModel.NormalUser;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String _id;
    private String productName;
    private List<String> images;
    private String category;
    private double originalPrice;
    private String condition;
    private double purchasePrice;
    private double sellPrice;
    private String shortDescription;
    private String detailedDescription;
    private String userId;  // Keep as string for simplicity
    private NormalUser user;      // Optional user object when populated
    private String status;
    private String createdAt;
    private String updatedAt;
    private String rejectionReason;

    // Constructor
    public Product() {}

    // Getters and setters
    public String getId() { return _id; }
    public void setId(String id) { this._id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public NormalUser getUser() { return user; }
    public void setUser(NormalUser user) { this.user = user; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    // Helper methods
    public String getUserName() {
        return user != null ? user.getUsername() : null;
    }

    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }

    public String getUserPhone() {
        return user != null ? user.getPhoneNumber() : null;
    }
}