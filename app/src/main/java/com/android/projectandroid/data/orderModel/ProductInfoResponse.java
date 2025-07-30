package com.android.projectandroid.data.orderModel;

// ===== ProductInfoResponse.java =====
public class ProductInfoResponse {
    private boolean success;
    private String message;
    private ProductInfo data;

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

    public ProductInfo getData() {
        return data;
    }

    public void setData(ProductInfo data) {
        this.data = data;
    }

    // Inner class cho product info
    public static class ProductInfo {
        private String _id;
        private String productName;
        private java.util.List<String> images;
        private String category;
        private String status;
        private String detailedDescription;

        // Getters and Setters
        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public java.util.List<String> getImages() {
            return images;
        }

        public void setImages(java.util.List<String> images) {
            this.images = images;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDetailedDescription() {
            return detailedDescription;
        }

        public void setDetailedDescription(String detailedDescription) {
            this.detailedDescription = detailedDescription;
        }
    }
}
