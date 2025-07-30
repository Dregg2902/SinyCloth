package com.android.projectandroid.data.orderModel;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleOrder implements Serializable {
    private String _id;
    private String userId;
    private String productId;
    private double price;
    private String status;
    private String orderType;
    private String orderDate;
    private CustomerInfo customerInfo;
    private ProductSnapshot productSnapshot;
    private String notes;
    private String kg; // ✅ THÊM FIELD KG
    private String deliveredAt;
    private String cancelledAt;
    private String cancelReason;
    private String createdAt;
    private String updatedAt;

    // Nested classes
    public static class CustomerInfo implements Serializable {
        private String name;
        private String phoneNumber;
        private String address;

        public CustomerInfo() {}

        public CustomerInfo(String name, String phoneNumber, String address) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.address = address;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class ProductSnapshot implements Serializable {
        private String productName;
        private String productImage;
        private String category;

        public ProductSnapshot() {}

        public ProductSnapshot(String productName, String productImage, String category) {
            this.productName = productName;
            this.productImage = productImage;
            this.category = category;
        }

        // Getters and Setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    // Constructors
    public SimpleOrder() {}

    // Getters and Setters
    public String getId() { return _id; }
    public void setId(String id) { this._id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public CustomerInfo getCustomerInfo() { return customerInfo; }
    public void setCustomerInfo(CustomerInfo customerInfo) { this.customerInfo = customerInfo; }

    public ProductSnapshot getProductSnapshot() { return productSnapshot; }
    public void setProductSnapshot(ProductSnapshot productSnapshot) { this.productSnapshot = productSnapshot; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ✅ GETTER VÀ SETTER CHO KG
    public String getKg() { return kg; }
    public void setKg(String kg) { this.kg = kg; }

    public String getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // ✅ HELPER METHODS CHO 3 TRẠNG THÁI
    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price) + " VNĐ";
    }

    public String getStatusDisplayText() {
        if (status == null) return "Không xác định";

        switch (status.toLowerCase()) {
            case "shipping":
                return "Đang giao";
            case "delivered":
                return "Đã giao";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    public String getOrderTypeDisplayText() {
        if (orderType == null) return "Không xác định";

        switch (orderType.toLowerCase()) {
            case "pass_pickup":
                return "Đi lấy đồ pass";
            case "donation_pickup":
                return "Đi lấy đồ quyên góp";
            case "delivery":
                return "Đi giao hàng";
            default:
                return "Không xác định";
        }
    }

    // ✅ MÀU SẮC CHO 3 TRẠNG THÁI
    public int getStatusColor() {
        if (status == null) return 0xFF757575;

        switch (status.toLowerCase()) {
            case "shipping":
                return 0xFF2196F3; // Blue - đang giao
            case "delivered":
                return 0xFFFFFFFF; // White - đã giao
            case "cancelled":
                return 0xFFEF5350; // Red - đã hủy
            default:
                return 0xFF757575; // Grey
        }
    }

    public int getOrderTypeColor() {
        if (orderType == null) return 0xFF757575;

        switch (orderType.toLowerCase()) {
            case "pass_pickup":
                return 0xFF2196F3; // Blue
            case "donation_pickup":
                return 0xFF4CAF50; // Green
            case "delivery":
                return 0xFFFF9800; // Orange
            default:
                return 0xFF757575; // Grey
        }
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(orderDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

                Date date = fallbackFormat.parse(orderDate);
                return outputFormat.format(date);
            } catch (ParseException ex) {
                return orderDate;
            }
        }
    }

    // ✅ CHỈ CÓ THỂ HỦY KHI ĐANG GIAO
    public boolean canCancel() {
        return status != null && "shipping".equals(status);
    }

    // ✅ KIỂM TRA TRẠNG THÁI CỤ THỂ
    public boolean isShipping() {
        return "shipping".equals(status);
    }

    public boolean isDelivered() {
        return "delivered".equals(status);
    }

    public boolean isCancelled() {
        return "cancelled".equals(status);
    }

    // ✅ KIỂM TRA CÓ PHẢI QUYÊN GÓP KHÔNG
    public boolean isDonationOrder() {
        return "donation_pickup".equals(orderType);
    }

    // ✅ GET ICON CHO TRẠNG THÁI
    public String getStatusIcon() {
        if (status == null) return "❓";

        switch (status.toLowerCase()) {
            case "shipping":
                return "🚚"; // Truck - đang giao
            case "delivered":
                return "✅"; // Check mark - đã giao
            case "cancelled":
                return "❌"; // X mark - đã hủy
            default:
                return "❓";
        }
    }

    // ✅ GET FORMATTED DELIVERY/CANCEL TIME
    public String getFormattedDeliveredTime() {
        if (deliveredAt == null) return "";
        return formatTimestamp(deliveredAt);
    }

    public String getFormattedCancelledTime() {
        if (cancelledAt == null) return "";
        return formatTimestamp(cancelledAt);
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }

    // Helper getters for easy access
    public String getProductName() {
        return productSnapshot != null ? productSnapshot.getProductName() : "";
    }

    public String getProductImage() {
        return productSnapshot != null ? productSnapshot.getProductImage() : null;
    }

    public String getProductCategory() {
        return productSnapshot != null ? productSnapshot.getCategory() : "";
    }

    public String getCustomerName() {
        return customerInfo != null ? customerInfo.getName() : "";
    }

    public String getCustomerPhone() {
        return customerInfo != null ? customerInfo.getPhoneNumber() : "";
    }

    public String getCustomerAddress() {
        return customerInfo != null ? customerInfo.getAddress() : "";
    }

    // ✅ GET STATUS DESCRIPTION WITH DETAILS
    public String getDetailedStatusText() {
        switch (status.toLowerCase()) {
            case "shipping":
                return "Đơn hàng đang được giao đến bạn";
            case "delivered":
                String deliveredTime = getFormattedDeliveredTime();
                String result = "Đã giao thành công" + (!deliveredTime.isEmpty() ? " lúc " + deliveredTime : "");

                // ✅ THÊM THÔNG TIN ĐIỂM THƯỞNG CHO QUYÊN GÓP
                if (isDonationOrder() && kg != null && !kg.isEmpty()) {
                    try {
                        double kgValue = Double.parseDouble(kg);
                        int points = (int) (kgValue * 1000);
                        result += " - Bạn đã nhận được " + points + " điểm thưởng!";
                    } catch (NumberFormatException e) {
                        // Ignore parsing error
                    }
                }
                return result;
            case "cancelled":
                String cancelledTime = getFormattedCancelledTime();
                String reason = cancelReason != null ? " - " + cancelReason : "";
                return "Đã hủy" + (!cancelledTime.isEmpty() ? " lúc " + cancelledTime : "") + reason;
            default:
                return "Trạng thái không xác định";
        }
    }

    // ✅ GET FORMATTED KG INFO
    public String getFormattedKgInfo() {
        if (kg == null || kg.isEmpty()) return "";

        try {
            double kgValue = Double.parseDouble(kg);
            DecimalFormat formatter = new DecimalFormat("#.#");
            return formatter.format(kgValue) + " kg";
        } catch (NumberFormatException e) {
            return kg + " kg";
        }
    }

    // ✅ TÍNH ĐIỂM THƯỞNG CHO QUYÊN GÓP
    public int calculateRewardPoints() {
        if (!isDonationOrder() || kg == null || kg.isEmpty()) {
            return 0;
        }

        try {
            double kgValue = Double.parseDouble(kg);
            return (int) (kgValue * 1000); // 1000 điểm cho 1kg
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ✅ HIỂN THỊ THÔNG TIN QUYÊN GÓP
    public String getDonationInfo() {
        if (!isDonationOrder()) return "";

        StringBuilder info = new StringBuilder();

        if (kg != null && !kg.isEmpty()) {
            info.append("Số lượng: ").append(getFormattedKgInfo());
        }

        if (notes != null && !notes.isEmpty()) {
            if (info.length() > 0) info.append(" - ");
            info.append("Lời chúc: ").append(notes);
        }

        return info.toString();
    }

    @Override
    public String toString() {
        return "SimpleOrder{" +
                "_id='" + _id + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                ", orderType='" + orderType + '\'' +
                ", kg='" + kg + '\'' +
                ", orderDate='" + orderDate + '\'' +
                '}';
    }
}