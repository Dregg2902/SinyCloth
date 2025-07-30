package com.android.projectandroid.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.GetOrdersResponse;
import com.android.projectandroid.data.orderModel.SimpleOrder;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.userModel.UserPointsResponse;
import com.android.projectandroid.data.userModel.DonationHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity hiển thị lịch sử quyên góp và thống kê điểm
 */
public class DonationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvDonationHistory;
    private TextView tvTotalDonations, tvTotalKg, tvTotalPoints, tvEmptyState;
    private View layoutStats, layoutEmpty;

    private DonationHistoryAdapter adapter;
    private String userId;
    private ProgressDialog progressDialog;
    private ImageView Back;
    private Button btnStartDonating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.donation_history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDonationHistory();
        loadUserPoints();
    }

    private void initViews() {
        rvDonationHistory = findViewById(R.id.rvDonationHistory);
        tvTotalDonations = findViewById(R.id.tvTotalDonations);
        tvTotalKg = findViewById(R.id.tvTotalKg);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        layoutStats = findViewById(R.id.layoutStats);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        Back = findViewById(R.id.btnBack);
        btnStartDonating = findViewById(R.id.btnStartDonating);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải lịch sử...");
        progressDialog.setCancelable(false);
    }

    private void setupRecyclerView() {
        adapter = new DonationHistoryAdapter(new ArrayList<>());
        rvDonationHistory.setLayoutManager(new LinearLayoutManager(this));
        rvDonationHistory.setAdapter(adapter);
        Back.setOnClickListener(v -> onBackPressed());
        btnStartDonating.setOnClickListener(v -> StartDonating());
    }

    private void StartDonating() {
        Intent intent = new Intent(DonationHistoryActivity.this, BottomNavigation.class);
        intent.putExtra("targetFragment", "notice");  // "notice" chính là case trong handleIntent
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loadDonationHistory() {
        progressDialog.show();

        // Lấy tất cả đơn hàng của user với filter donation_pickup
        Call<GetOrdersResponse> call = SimpleOrderApiService.orderApiService.getUserOrders(userId, null);
        call.enqueue(new Callback<GetOrdersResponse>() {
            @Override
            public void onResponse(Call<GetOrdersResponse> call, Response<GetOrdersResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    GetOrdersResponse ordersResponse = response.body();
                    if (ordersResponse.isSuccess()) {
                        List<SimpleOrder> allOrders = ordersResponse.getData();

                        // Filter chỉ lấy đơn quyên góp
                        List<SimpleOrder> donationOrders = filterDonationOrders(allOrders);

                        if (donationOrders.isEmpty()) {
                            showEmptyState();
                        } else {
                            showDonationHistory(donationOrders);
                        }
                    } else {
                        showError("Không thể tải lịch sử: " + ordersResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải lịch sử quyên góp");
                }
            }

            @Override
            public void onFailure(Call<GetOrdersResponse> call, Throwable t) {
                progressDialog.dismiss();
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadUserPoints() {
        Call<UserPointsResponse> call = SimpleOrderApiService.orderApiService.getUserPoints(userId);
        call.enqueue(new Callback<UserPointsResponse>() {
            @Override
            public void onResponse(Call<UserPointsResponse> call, Response<UserPointsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserPointsResponse pointsResponse = response.body();
                    if (pointsResponse.isSuccess()) {
                        updateStatsUI(pointsResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserPointsResponse> call, Throwable t) {
                // Không hiển thị lỗi cho việc load stats
            }
        });
    }

    private List<SimpleOrder> filterDonationOrders(List<SimpleOrder> allOrders) {
        List<SimpleOrder> donationOrders = new ArrayList<>();
        for (SimpleOrder order : allOrders) {
            if ("donation_pickup".equals(order.getOrderType())) {
                donationOrders.add(order);
            }
        }
        return donationOrders;
    }

    private void showDonationHistory(List<SimpleOrder> donationOrders) {
        layoutEmpty.setVisibility(View.GONE);
        layoutStats.setVisibility(View.VISIBLE);
        rvDonationHistory.setVisibility(View.VISIBLE);

        adapter.updateData(donationOrders);
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutStats.setVisibility(View.GONE);
        rvDonationHistory.setVisibility(View.GONE);

        tvEmptyState.setText("🎁\n\nBạn chưa có đơn quyên góp nào\n\nHãy bắt đầu quyên góp để giúp đỡ cộng đồng và tích lũy điểm thưởng!");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showEmptyState();
    }

    private void updateStatsUI(UserPointsResponse.UserPointsData data) {
        if (data.getDonationHistory() != null) {
            UserPointsResponse.DonationHistory history = data.getDonationHistory();

            tvTotalDonations.setText(String.valueOf(history.getTotalOrders()));
            tvTotalKg.setText(history.getFormattedTotalKg());
            tvTotalPoints.setText(history.getFormattedTotalPoints());
        }
    }

    /**
     * Adapter cho RecyclerView hiển thị lịch sử quyên góp
     */
    private static class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder> {

        private List<SimpleOrder> orders;

        public DonationHistoryAdapter(List<SimpleOrder> orders) {
            this.orders = orders;
        }

        public void updateData(List<SimpleOrder> newOrders) {
            this.orders = newOrders;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donation_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SimpleOrder order = orders.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvProductName, tvKg, tvStatus, tvDate, tvPoints, tvWishMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvKg = itemView.findViewById(R.id.tvKg);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvPoints = itemView.findViewById(R.id.tvPoints);
                tvWishMessage = itemView.findViewById(R.id.tvWishMessage);
            }

            public void bind(SimpleOrder order) {
                // Tên sản phẩm
                tvProductName.setText(order.getProductName());

                // Số kg
                tvKg.setText(order.getFormattedKgInfo());

                // Trạng thái
                tvStatus.setText(order.getStatusDisplayText());
                tvStatus.setTextColor(order.getStatusColor());

                // Ngày
                tvDate.setText(order.getFormattedOrderDate());

                // Điểm thưởng
                if (order.isDelivered() && order.isDonationOrder()) {
                    int points = order.calculateRewardPoints();
                    tvPoints.setText("+" + points + " điểm");
                    tvPoints.setVisibility(View.VISIBLE);
                } else if (order.isDonationOrder()) {
                    int expectedPoints = order.calculateRewardPoints();
                    tvPoints.setText("Dự kiến: +" + expectedPoints + " điểm");
                    tvPoints.setVisibility(View.VISIBLE);
                    tvPoints.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                } else {
                    tvPoints.setVisibility(View.GONE);
                }

                // Lời chúc
                if (order.getNotes() != null && !order.getNotes().isEmpty()) {
                    tvWishMessage.setText("💌 " + order.getNotes());
                    tvWishMessage.setVisibility(View.VISIBLE);
                } else {
                    tvWishMessage.setVisibility(View.GONE);
                }
            }
        }
    }
}