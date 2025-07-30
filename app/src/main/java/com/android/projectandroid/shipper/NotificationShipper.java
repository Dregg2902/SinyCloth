package com.android.projectandroid.shipper;

import android.os.Bundle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.projectandroid.R;
import com.android.projectandroid.shipper.NotificationAdapter;
import com.android.projectandroid.shipper.NotificationItem;
import com.android.projectandroid.shipper.NotificationManager;
import java.util.ArrayList;
import java.util.List;

public class NotificationShipper extends Fragment implements NotificationManager.NotificationListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;
    private NotificationManager notificationManager;

    public NotificationShipper() {
        // Required empty public constructor
    }

    public static NotificationShipper newInstance(String param1, String param2) {
        NotificationShipper fragment = new NotificationShipper();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Khởi tạo notification manager
        notificationManager = NotificationManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_shipper, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.notificationshipper), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo RecyclerView
        setupRecyclerView(view);

        // Load dữ liệu thông báo
        loadNotifications();

        return view;
    }

    private void setupRecyclerView(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        notificationList = new ArrayList<>();

        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        rvNotifications.setAdapter(notificationAdapter);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        // Thêm ItemDecoration cho khoảng cách giữa các item
        rvNotifications.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
                getContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
    }

    private void loadNotifications() {
        // Load tất cả thông báo từ NotificationManager
        List<NotificationItem> allNotifications = notificationManager.getAllNotifications();
        notificationList.clear();
        notificationList.addAll(allNotifications);
        notificationAdapter.notifyDataSetChanged();

//        // Thêm một số thông báo mẫu nếu danh sách trống (chỉ để demo)
//        if (notificationList.isEmpty()) {
//            addSampleNotifications();
//        }
    }

    private void addSampleNotifications() {
//        // Thêm thông báo mẫu như trong hình
//        NotificationItem successNotification = new NotificationItem(
//                "Lấy hàng thành công",
//                "Bạn đã lấy hàng thành công của khách hàng ở địa điểm Võ Thị Sáu, Quận 1, TP.HCM.",
//                true,
//                "ORDER001",
//                "Võ Thị Sáu, Quận 1, TP.HCM"
//        );
//
//        NotificationItem failedNotification = new NotificationItem(
//                "Lấy hàng thất bại",
//                "Bạn chưa thể lấy hàng của khách hàng ở địa điểm 720A Điện Biên Phủ, Vinhomes Tân Cảng, Bình Thạnh, TP.HCM.",
//                false,
//                "ORDER002",
//                "720A Điện Biên Phủ, Vinhomes Tân Cảng, Bình Thạnh, TP.HCM"
//        );
//
//        notificationManager.addNotification(successNotification);
//        notificationManager.addNotification(failedNotification);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Đăng ký listener khi fragment được hiển thị
        notificationManager.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hủy đăng ký listener khi fragment không hiển thị
        notificationManager.removeListener(this);
    }

    // Implement NotificationListener methods
    @Override
    public void onNotificationAdded(NotificationItem notification) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                notificationList.add(0, notification);
                notificationAdapter.notifyItemInserted(0);
                rvNotifications.scrollToPosition(0);
            });
        }
    }

    @Override
    public void onNotificationsUpdated(List<NotificationItem> notifications) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                notificationList.clear();
                notificationList.addAll(notifications);
                notificationAdapter.notifyDataSetChanged();
            });
        }
    }
}