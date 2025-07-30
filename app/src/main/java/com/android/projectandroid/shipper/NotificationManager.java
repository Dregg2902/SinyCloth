package com.android.projectandroid.shipper;

import com.android.projectandroid.shipper.NotificationItem;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static NotificationManager instance;
    private List<NotificationItem> notifications;
    private List<NotificationListener> listeners;

    public interface NotificationListener {
        void onNotificationAdded(NotificationItem notification);
        void onNotificationsUpdated(List<NotificationItem> notifications);
    }

    private NotificationManager() {
        notifications = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void addNotification(NotificationItem notification) {
        notifications.add(0, notification); // Thêm vào đầu danh sách

        // Thông báo cho tất cả listeners
        for (NotificationListener listener : listeners) {
            listener.onNotificationAdded(notification);
        }
    }

    public List<NotificationItem> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    // Phương thức tiện ích để tạo thông báo giao hàng thành công
    public void addSuccessDeliveryNotification(String orderId, String address, String timestamp) {
        String title = "Giao hàng thành công";
        String message = "Bạn đã giao hàng thành công cho khách hàng ở địa điểm " + address;

        NotificationItem notification = new NotificationItem(title, message, true, orderId, address ,timestamp);
        addNotification(notification);
    }

    // Phương thức tiện ích để tạo thông báo giao hàng thất bại
    public void addFailedDeliveryNotification(String orderId, String address, String reason,String timestamp) {
        String title = "Giao hàng thất bại";
        String message = "Bạn chưa thể giao hàng của khách hàng ở địa điểm " + address +
                (reason != null && !reason.isEmpty() ? ". Lý do: " + reason : "");

        NotificationItem notification = new NotificationItem(title, message, false, orderId, address ,timestamp);
        addNotification(notification);
    }

    // Phương thức tiện ích để tạo thông báo lấy hàng thành công
    public void addSuccessPickupNotification(String orderId, String address, String timestamp) {
        String title = "Lấy hàng thành công";
        String message = "Bạn đã lấy hàng thành công của khách hàng ở địa điểm " + address;

        NotificationItem notification = new NotificationItem(title, message, true, orderId, address, timestamp);
        addNotification(notification);
    }

    // Phương thức tiện ích để tạo thông báo lấy hàng thất bại
    public void addFailedPickupNotification(String orderId, String address, String reason, String timestamp) {
        String title = "Lấy hàng thất bại";
        String message = "Bạn chưa thể lấy hàng của khách hàng ở địa điểm " + address +
                (reason != null && !reason.isEmpty() ? ". Lý do: " + reason : "");

        NotificationItem notification = new NotificationItem(title, message, false, orderId, address,timestamp);
        addNotification(notification);
    }

    public void clearAllNotifications() {
        notifications.clear();
        for (NotificationListener listener : listeners) {
            listener.onNotificationsUpdated(notifications);
        }
    }
}
