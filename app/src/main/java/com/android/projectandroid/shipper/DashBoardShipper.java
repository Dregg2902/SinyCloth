package com.android.projectandroid.shipper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.ShipperWeeklyStatsResponse;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashBoardShipper extends Fragment {

    private static final String TAG = "DashBoardShipper";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Views
    private BarChart barChartOrders;
    private PieChart pieChartSuccess;
    private TextView tvCurrentWeek;
    private TextView tvSuccessRate;
    private TextView tvFailureRate;
    private TextView tvTotalOrders;
    private TextView tvDeliveredOrders;
    private ImageButton btnPreviousWeek;
    private ImageButton btnNextWeek;

    // Data
    private int currentWeekOffset = 0; // 0 = tuần hiện tại, -1 = tuần trước, +1 = tuần sau
    private Calendar currentWeek;
    private String shipperId;

    // API Service
    private SimpleOrderApiService apiService;
    private PreferenceManager preferenceManager;

    public DashBoardShipper() {
        // Required empty public constructor
    }

    public static DashBoardShipper newInstance(String param1, String param2) {
        DashBoardShipper fragment = new DashBoardShipper();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentWeek = Calendar.getInstance();
        apiService = SimpleOrderApiService.orderApiService;
        preferenceManager = new PreferenceManager(getContext());
        // Lấy shipperId từ SharedPreferences hoặc arguments
        getShipperId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dash_board_shipper, container, false);

        // Áp dụng WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initViews(view);

        // Setup charts
        setupBarChart();
        setupPieChart();

        // Setup navigation
        setupWeekNavigation();

        // Load initial data from API
        loadWeeklyDataFromAPI();

        return view;
    }

    private void getShipperId() {
        NormalUser currentuser = preferenceManager.getNormalUser();
        if (getArguments() != null) {
            shipperId = getArguments().getString("shipperId");
        }

        // Nếu không có trong arguments, lấy từ SharedPreferences
        if (shipperId == null || shipperId.isEmpty()) {
            SharedPreferences prefs = requireContext().getSharedPreferences("shipper_prefs", Context.MODE_PRIVATE);
            shipperId = prefs.getString("shipper_id", "");
        }

        // Nếu vẫn không có, sử dụng ID mặc định hoặc hiển thị lỗi
        if (shipperId == null || shipperId.isEmpty()) {
            shipperId = currentuser.get_id(); // Thay bằng logic lấy ID thực tế
            Log.w(TAG, "ShipperId not found, using default");
        }
    }

    private void initViews(View view) {
        barChartOrders = view.findViewById(R.id.barChartOrders);
        pieChartSuccess = view.findViewById(R.id.pieChartSuccess);
        tvCurrentWeek = view.findViewById(R.id.tvCurrentWeek);
        tvSuccessRate = view.findViewById(R.id.tvSuccessRate);
        tvFailureRate = view.findViewById(R.id.tvFailureRate);
//        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
//        tvDeliveredOrders = view.findViewById(R.id.tvDeliveredOrders);
        btnPreviousWeek = view.findViewById(R.id.btnPreviousWeek);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);
    }

    private void setupBarChart() {
        barChartOrders.getDescription().setEnabled(false);
        barChartOrders.setDrawGridBackground(false);
        barChartOrders.setDrawBarShadow(false);
        barChartOrders.setHighlightFullBarEnabled(false);
        barChartOrders.setPinchZoom(false);
        barChartOrders.setDrawValueAboveBar(true);

        // Customize X axis
        XAxis xAxis = barChartOrders.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        // Customize Y axes
        barChartOrders.getAxisLeft().setDrawGridLines(false);
        barChartOrders.getAxisRight().setEnabled(false);
        barChartOrders.getAxisLeft().setAxisMinimum(0f);

        // Remove legend
        barChartOrders.getLegend().setEnabled(false);

        // Animation
        barChartOrders.animateY(1000);
    }

    private void setupPieChart() {
        pieChartSuccess.getDescription().setEnabled(false);
        pieChartSuccess.setDrawHoleEnabled(true);
        pieChartSuccess.setHoleColor(Color.WHITE);
        pieChartSuccess.setHoleRadius(58f);
        pieChartSuccess.setDrawCenterText(false);
        pieChartSuccess.setRotationAngle(0);
        pieChartSuccess.setRotationEnabled(false);
        pieChartSuccess.setHighlightPerTapEnabled(true);

        // Remove legend
        pieChartSuccess.getLegend().setEnabled(false);

        // Animation
        pieChartSuccess.animateY(1000);
    }

    private void setupWeekNavigation() {
        btnPreviousWeek.setOnClickListener(v -> {
            currentWeekOffset--;
            loadWeeklyDataFromAPI();
            updateWeekDisplay();
        });

        btnNextWeek.setOnClickListener(v -> {
            if (currentWeekOffset < 0) { // Chỉ cho phép đi về tương lai nếu đang ở quá khứ
                currentWeekOffset++;
                loadWeeklyDataFromAPI();
                updateWeekDisplay();
            }
        });

        updateWeekDisplay();
    }

    private void updateWeekDisplay() {
        String weekText;
        if (currentWeekOffset == 0) {
            weekText = "Tuần này";
        } else if (currentWeekOffset == -1) {
            weekText = "Tuần trước";
        } else if (currentWeekOffset < -1) {
            weekText = "Tuần trước " + Math.abs(currentWeekOffset);
        } else {
            weekText = "Tuần sau " + currentWeekOffset;
        }

        tvCurrentWeek.setText(weekText);

        // Enable/disable next button
        btnNextWeek.setEnabled(currentWeekOffset < 0);
        btnNextWeek.setAlpha(currentWeekOffset < 0 ? 1.0f : 0.5f);
    }

    private void loadWeeklyDataFromAPI() {
        if (shipperId == null || shipperId.isEmpty()) {
            Log.e(TAG, "ShipperId is null or empty");
            showErrorAndLoadSampleData("ShipperId không hợp lệ");
            return;
        }

        Call<ShipperWeeklyStatsResponse> call = apiService.getShipperWeeklyStats(shipperId, currentWeekOffset);

        call.enqueue(new Callback<ShipperWeeklyStatsResponse>() {
            @Override
            public void onResponse(Call<ShipperWeeklyStatsResponse> call, Response<ShipperWeeklyStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShipperWeeklyStatsResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        updateUIWithAPIData(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API response success=false");
                        showErrorAndLoadSampleData("Dữ liệu không hợp lệ");
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code() + " " + response.message());
                    showErrorAndLoadSampleData("Lỗi kết nối API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ShipperWeeklyStatsResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                showErrorAndLoadSampleData("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateUIWithAPIData(ShipperWeeklyStatsResponse.WeeklyData data) {
        // Update bar chart với dữ liệu thực
        updateBarChart(data.getWeekData());

        // Update pie chart và text views với thống kê thực
        if (data.getStats() != null) {
            updatePieChart(data.getStats());
            updateStatistics(data.getStats());
        }
    }

    private void updateBarChart(List<Integer> weekData) {
        List<BarEntry> entries = new ArrayList<>();
        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        // Sử dụng dữ liệu từ API
        for (int i = 0; i < Math.min(7, weekData.size()); i++) {
            entries.add(new BarEntry(i, weekData.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Đơn hàng");

        // Set colors - gradient from light green to yellow
        int[] colors = {
                Color.rgb(144, 238, 144), // Light green
                Color.rgb(152, 251, 152), // Pale green
                Color.rgb(173, 255, 47),  // Green yellow
                Color.rgb(154, 205, 50),  // Yellow green
                Color.rgb(255, 255, 0),   // Yellow
                Color.rgb(255, 215, 0),   // Gold
                Color.rgb(255, 165, 0)    // Orange
        };
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // Set custom labels for X axis
        XAxis xAxis = barChartOrders.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));

        barChartOrders.setData(barData);
        barChartOrders.invalidate();
    }

    private void updatePieChart(ShipperWeeklyStatsResponse.WeeklyStats stats) {
        List<PieEntry> entries = new ArrayList<>();

        float successRate = stats.getSuccessRate();
        float failureRate = stats.getFailureRate();

        if (successRate > 0) {
            entries.add(new PieEntry(successRate));
        }
        if (failureRate > 0) {
            entries.add(new PieEntry(failureRate));
        }

        // Nếu có dữ liệu khác (100% - success - failure)
        float otherRate = 100 - successRate - failureRate;

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);  // ví dụ: 65.3%
            }
        });
        // Set colors
        List<Integer> colors = new ArrayList<>();
        if (otherRate > 0 && successRate ==0 && failureRate == 0) {
            colors.add(Color.rgb(158, 158, 158));  // Gray for others (giữ nguyên)
        }
        colors.add(Color.parseColor("#FF6B6B"));   // Red/pink for success
        colors.add(Color.parseColor("#FFA726"));   // Orange for failure


        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChartSuccess.setData(pieData);
        pieChartSuccess.invalidate();
    }

    private void updateStatistics(ShipperWeeklyStatsResponse.WeeklyStats stats) {
        // Update text views với dữ liệu thực
        tvSuccessRate.setText(String.format("%.1f%%", stats.getSuccessRate()));
        tvFailureRate.setText(String.format("%.1f%%", stats.getFailureRate()));

        if (tvTotalOrders != null) {
            tvTotalOrders.setText(String.valueOf(stats.getTotalOrders()));
        }
        if (tvDeliveredOrders != null) {
            tvDeliveredOrders.setText(String.valueOf(stats.getDelivered()));
        }
    }

    private void showErrorAndLoadSampleData(String errorMessage) {
        if (getContext() != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        // Load sample data as fallback
        loadSampleData();
    }

    private void loadSampleData() {
        // Tạo dữ liệu mẫu cho bar chart
        List<Integer> sampleWeekData = new ArrayList<>();
        int[] baseData = {12, 8, 18, 15, 10, 25, 9};

        // Thay đổi dữ liệu dựa trên tuần được chọn
        for (int i = 0; i < baseData.length; i++) {
            int value = baseData[i] + (currentWeekOffset * 2);
            if (value < 0) value = 0;
            sampleWeekData.add(value);
        }

        updateBarChart(sampleWeekData);

        // Tạo dữ liệu mẫu cho pie chart và statistics
        ShipperWeeklyStatsResponse.WeeklyStats sampleStats = createSampleStats();
        updatePieChart(sampleStats);
        updateStatistics(sampleStats);
    }

    private ShipperWeeklyStatsResponse.WeeklyStats createSampleStats() {
        ShipperWeeklyStatsResponse.WeeklyStats stats = new ShipperWeeklyStatsResponse.WeeklyStats();
        stats.setSuccessRate(70.0f);
        stats.setFailureRate(21.0f);
        stats.setTotalOrders(45);
        stats.setDelivered(32);
        stats.setCancelled(9);
        return stats;
    }
}