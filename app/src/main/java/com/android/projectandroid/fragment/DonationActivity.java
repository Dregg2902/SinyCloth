package com.android.projectandroid.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.CreateOrderRequest;
import com.android.projectandroid.data.orderModel.CreateOrderResponse;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.userModel.UserPointsResponse;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PointsUtils;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.user.DonationHistoryActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để tạo đơn quyên góp với tính năng tính điểm
 */
public class DonationActivity extends Fragment {

    private EditText etKg, etWishMessage;
    private TextView tvExpectedPoints, tvCurrentPoints, tvUserLevel, tvKgToNextLevel;
    private Button btnCreateDonation, btnViewHistory;
    private CardView cardUserStats;

    private String productId; // ✅ OPTIONAL - có thể null cho standalone donation
    private NormalUser currentUser;
    private PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getContext());
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_donation, container, false);

        // Đảm bảo padding cho view theo hệ thống (status bar, nav bar) động
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.donation), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews(view);
        loadUserData();
        setupListeners();
        loadUserPoints();

        // ✅ DONATION CÓ THỂ HOẠT ĐỘNG STANDALONE KHÔNG CẦN PRODUCT
        if (productId == null) {
            // Standalone donation mode
            requireActivity().setTitle("💝 Quyên Góp Cộng Đồng");
        } else {
            // Product-based donation mode
            requireActivity().setTitle("💝 Quyên Góp Sản Phẩm");
        }
        return view;
    }

    private void initViews(View view) {
        etKg = view.findViewById(R.id.etKg);
        etWishMessage = view.findViewById(R.id.etWishMessage);
        tvExpectedPoints = view.findViewById(R.id.tvExpectedPoints);
        tvCurrentPoints = view.findViewById(R.id.tvCurrentPoints);
        tvUserLevel = view.findViewById(R.id.tvUserLevel);
        tvKgToNextLevel = view.findViewById(R.id.tvKgToNextLevel);
        btnCreateDonation = view.findViewById(R.id.btnCreateDonation);
        btnViewHistory = view.findViewById(R.id.btnViewHistory);
        cardUserStats = view.findViewById(R.id.cardUserStats);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void loadUserData() {
        // Khởi tạo PreferenceManager

        // Lấy thông tin user từ PreferenceManager
        currentUser = preferenceManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ DEBUG: Log user data
        Log.d("DonationActivity", "Current user loaded:");
        Log.d("DonationActivity", "ID: " + currentUser.get_id());
        Log.d("DonationActivity", "Username: " + currentUser.getUsername());
        Log.d("DonationActivity", "Phone: " + currentUser.getPhoneNumber());
        Log.d("DonationActivity", "Address: " + currentUser.getAddress());
        Log.d("DonationActivity", "Email: " + currentUser.getEmail());

        // ✅ KIỂM TRA VÀ CẢNH BÁO THIẾU THÔNG TIN
        boolean needsUpdate = false;
        StringBuilder missingInfo = new StringBuilder("Vui lòng cập nhật thông tin sau để có thể quyên góp:\n");

        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            missingInfo.append("• Số điện thoại\n");
            needsUpdate = true;
        }

        if (currentUser.getAddress() == null || currentUser.getAddress().isEmpty()) {
            missingInfo.append("• Địa chỉ\n");
            needsUpdate = true;
        }

        if (needsUpdate) {
            // Hiển thị cảnh báo và disable nút tạo đơn
            btnCreateDonation.setEnabled(false);
            btnCreateDonation.setText("Cần cập nhật thông tin");

            Toast.makeText(getContext(), missingInfo.toString(), Toast.LENGTH_LONG).show();

            // Có thể thêm dialog để chuyển đến trang cập nhật profile
            showUpdateProfileDialog();
        } else {
            btnCreateDonation.setEnabled(true);
            btnCreateDonation.setText("Tạo đơn quyên góp");
        }
    }
    private void showUpdateProfileDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Cần cập nhật thông tin")
                .setMessage("Bạn cần có số điện thoại và địa chỉ để tạo đơn quyên góp. Có muốn cập nhật ngay không?")
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    // Chuyển đến activity cập nhật profile
                    // Intent intent = new Intent(this, UpdateProfileActivity.class);
                    // startActivity(intent);
                    Toast.makeText(getContext(), "Vui lòng cập nhật trong phần Profile", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Để sau", null)
                .show();
    }

    private void setupListeners() {
        // TextWatcher cho EditText kg để tính điểm dự kiến
        etKg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateExpectedPoints();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút tạo đơn quyên góp
        btnCreateDonation.setOnClickListener(v -> createDonationOrder());

        // Nút xem lịch sử
        btnViewHistory.setOnClickListener(v -> viewDonationHistory());
    }

    private void calculateExpectedPoints() {
        String kgText = etKg.getText().toString().trim();
        if (kgText.isEmpty()) {
            tvExpectedPoints.setText("Nhập số kg để xem điểm dự kiến");
            tvExpectedPoints.setTextColor(getResources().getColor(android.R.color.darker_gray));
            return;
        }

        try {
            double kg = Double.parseDouble(kgText);
            if (kg <= 0) {
                tvExpectedPoints.setText("Số kg phải lớn hơn 0");
                tvExpectedPoints.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
            }

            int expectedPoints = PointsUtils.calculatePointsFromKg(kgText);
            tvExpectedPoints.setText("Điểm dự kiến: +" + PointsUtils.formatPoints(expectedPoints));
            tvExpectedPoints.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // Kiểm tra level up
            if (currentUser != null) {
                int newPoints = currentUser.getPoints() + expectedPoints;
                if (PointsUtils.isLevelUp(currentUser.getPoints(), newPoints)) {
                    String levelUpMsg = PointsUtils.getLevelUpMessage(currentUser.getPoints(), newPoints);
                    tvExpectedPoints.setText(tvExpectedPoints.getText() + "\n" + levelUpMsg);
                }
            }

        } catch (NumberFormatException e) {
            tvExpectedPoints.setText("Số kg không hợp lệ");
            tvExpectedPoints.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void loadUserPoints() {
        if (currentUser == null) return;

        progressDialog.setMessage("Đang tải thông tin điểm...");
        progressDialog.show();

        Call<UserPointsResponse> call = SimpleOrderApiService.orderApiService.getUserPoints(currentUser.get_id());
        call.enqueue(new Callback<UserPointsResponse>() {
            @Override
            public void onResponse(Call<UserPointsResponse> call, Response<UserPointsResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    UserPointsResponse pointsResponse = response.body();
                    if (pointsResponse.isSuccess()) {
                        updateUserStatsUI(pointsResponse.getData());
                    } else {
                        Toast.makeText(getContext(),
                                pointsResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Không thể tải thông tin điểm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserPointsResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getContext(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserStatsUI(UserPointsResponse.UserPointsData data) {
        // Cập nhật điểm hiện tại
        currentUser.setPoints(data.getCurrentPoints());
        tvCurrentPoints.setText("Điểm hiện tại: " + data.getFormattedPoints());

        // Cập nhật level
        String level = data.getPointLevel();
        tvUserLevel.setText("Cấp độ: " + level);
        tvUserLevel.setTextColor(currentUser.getPointLevelColor());

        // Tính kg cần thiết để lên level tiếp theo
        int pointsNeeded = PointsUtils.getPointsNeededForNextLevel(data.getCurrentPoints());
        if (pointsNeeded > 0) {
            double kgNeeded = PointsUtils.calculateKgForPoints(pointsNeeded);
            tvKgToNextLevel.setText(String.format("Cần quyên góp thêm %.1f kg để lên cấp", kgNeeded));
            tvKgToNextLevel.setVisibility(View.VISIBLE);
        } else {
            tvKgToNextLevel.setVisibility(View.GONE);
        }

        // Hiển thị thống kê quyên góp
        if (data.getDonationHistory() != null) {
            UserPointsResponse.DonationHistory history = data.getDonationHistory();
            String statsText = String.format("Đã quyên góp: %d đơn • %s • %s điểm",
                    history.getTotalOrders(),
                    history.getFormattedTotalKg(),
                    history.getFormattedTotalPoints());

            // Có thể thêm TextView để hiển thị stats này
        }
    }

    private void createDonationOrder() {
        String kgText = etKg.getText().toString().trim();
        String wishMessage = etWishMessage.getText().toString().trim();

        // Validate
        if (kgText.isEmpty()) {
            etKg.setError("Vui lòng nhập số kg");
            return;
        }

        try {
            double kg = Double.parseDouble(kgText);
            if (kg <= 0) {
                etKg.setError("Số kg phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            etKg.setError("Số kg không hợp lệ");
            return;
        }

        if (wishMessage.isEmpty()) {
            etWishMessage.setError("Vui lòng nhập lời chúc");
            return;
        }

        // ✅ DEBUG: Kiểm tra thông tin user
        Log.d("DonationActivity", "User info check:");
        Log.d("DonationActivity", "User ID: " + currentUser.get_id());
        Log.d("DonationActivity", "Username: " + currentUser.getUsername());
        Log.d("DonationActivity", "Phone: " + currentUser.getPhoneNumber());
        Log.d("DonationActivity", "Address: " + currentUser.getAddress());

        // Kiểm tra user có đủ thông tin không
        if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng cập nhật số điện thoại trong profile trước khi quyên góp", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentUser.getAddress() == null || currentUser.getAddress().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng cập nhật địa chỉ trong profile trước khi quyên góp", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ TẠO REQUEST - PRODUCTID CÓ THỂ NULL CHO STANDALONE DONATION
        CreateOrderRequest request = new CreateOrderRequest(
                currentUser.get_id(),
                productId,  // ✅ Có thể null
                "donation_pickup",
                wishMessage,
                kgText
        );

        // ✅ DEBUG: Log request data
        Log.d("DonationActivity", "Creating order request:");
        Log.d("DonationActivity", "UserId: " + request.getUserId());
        Log.d("DonationActivity", "ProductId: " + request.getProductId());
        Log.d("DonationActivity", "OrderType: " + request.getOrderType());
        Log.d("DonationActivity", "Notes: " + request.getNotes());
        Log.d("DonationActivity", "Kg: " + request.getKg());

        progressDialog.setMessage("Đang tạo đơn quyên góp...");
        progressDialog.show();

        Call<CreateOrderResponse> call = SimpleOrderApiService.orderApiService.createOrder(request);
        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                progressDialog.dismiss();

                // ✅ DEBUG: Log full response
                Log.d("DonationActivity", "Response code: " + response.code());
                Log.d("DonationActivity", "Response message: " + response.message());

                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("DonationActivity", "Error body: " + errorBody);

                        // Hiển thị lỗi chi tiết từ server
                        Toast.makeText(getContext(),
                                "Lỗi server: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("DonationActivity", "Cannot read error body", e);
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    CreateOrderResponse orderResponse = response.body();
                    Log.d("DonationActivity", "Order response success: " + orderResponse.isSuccess());

                    if (orderResponse.isSuccess()) {
                        showSuccessMessage(orderResponse.getData());
                    } else {
                        Log.e("DonationActivity", "Order creation failed: " + orderResponse.getMessage());
                        Toast.makeText(getContext(),
                                "Lỗi tạo đơn: " + orderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("DonationActivity", "HTTP Error: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(),
                            "Lỗi HTTP " + response.code() + ": " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("DonationActivity", "Network error", t);
                Toast.makeText(getContext(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessMessage(CreateOrderResponse.OrderData orderData) {
        String message = "✅ Tạo đơn quyên góp thành công!\n\n";

        if (orderData.getExpectedRewardPoints() != null && orderData.getExpectedRewardPoints() > 0) {
            message += "🎁 " + orderData.getRewardMessage();
        }

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("success", true);
        resultIntent.putExtra("donation_id", orderData.getId());

        if (orderData.getExpectedRewardPoints() != null) {
            resultIntent.putExtra("points_awarded", orderData.getExpectedRewardPoints());
        }

        resultIntent.putExtra("kg_donated", etKg.getText().toString() + " kg");
        requireActivity().setResult(Activity.RESULT_OK, resultIntent);

    }

    private void viewDonationHistory() {
        // Chuyển đến Activity xem lịch sử quyên góp
        Intent intent = new Intent(getContext(), DonationHistoryActivity.class);
        intent.putExtra("userId", currentUser.get_id());
        startActivity(intent);
    }

}