package com.android.projectandroid.data.orderModel;

import androidx.annotation.NonNull;

import com.android.projectandroid.data.userModel.UserPointsResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SimpleOrderApiService {

    String BASE_URL = "https://modern-outgoing-quail.ngrok-free.app/api/";

    // ===== ORDER MANAGEMENT APIS - NO AUTHENTICATION REQUIRED =====

    // Tạo đơn hàng mới (đã hỗ trợ kg cho quyên góp)
    @POST("orders")
    Call<CreateOrderResponse> createOrder(
            @Body CreateOrderRequest request
    );

    // Lấy đơn hàng của user cụ thể
    @GET("orders/user/{userId}")
    Call<GetOrdersResponse> getUserOrders(
            @Path("userId") String userId,
            @Query("status") String status
    );

    // ✅ MỚI: Lấy điểm và lịch sử quyên góp của user
    @GET("orders/user/{userId}/points")
    Call<UserPointsResponse> getUserPoints(
            @Path("userId") String userId
    );

    // Lấy thống kê đơn hàng (đã bao gồm thống kê quyên góp)
    @GET("orders/stats/overview")
    Call<OrderStatisticsResponse> getOrderStatistics();

    // Lấy thông tin product riêng biệt
    @GET("orders/product/{productId}")
    Call<ProductInfoResponse> getProductInfo(
            @Path("productId") String productId
    );

    // Lấy chi tiết đơn hàng
    @GET("orders/{orderId}")
    Call<GetOrderDetailResponse> getOrderById(
            @Path("orderId") String orderId
    );

    // Hủy đơn hàng
    @PUT("orders/{orderId}/cancel")
    Call<UpdateOrderResponse> cancelOrder(
            @Path("orderId") String orderId,
            @Body CancelOrderRequest request
    );

    // Cập nhật trạng thái đơn hàng (đã hỗ trợ tự động tính điểm)
    @PUT("orders/{orderId}/status")
    Call<UpdateOrderResponse> updateOrderStatus(
            @Path("orderId") String orderId,
            @Body UpdateOrderStatusRequest request
    );

    // Lấy tất cả đơn hàng (admin)
    @GET("orders")
    Call<GetOrdersResponse> getAllOrders(
            @Query("status") String status,
            @Query("orderType") String orderType,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    // ===== SHIPPER APIS =====

    // Lấy tất cả đơn hàng của shipper và thống kê
    @GET("orders/shipper/{shipperId}/orders")
    Call<ShipperOrdersResponse> getShipperOrders(
            @Path("shipperId") String shipperId,
            @Query("status") String status,
            @Query("orderType") String orderType,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // Lấy thống kê dashboard theo tuần cho shipper
    @GET("orders/shipper/{shipperId}/weekly-stats")
    Call<ShipperWeeklyStatsResponse> getShipperWeeklyStats(
            @Path("shipperId") String shipperId,
            @Query("weekOffset") Integer weekOffset
    );


    // ===== UTILITY METHODS =====
    static int calculateExpectedPoints(String kg) {
        try {
            double kgValue = Double.parseDouble(kg);
            return (int) Math.floor(kgValue * 1000);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Kiểm tra đơn hàng có phải quyên góp không
     */
    static boolean isDonationOrder(String orderType) {
        return "donation_pickup".equals(orderType);
    }

    /**
     * Format điểm thành chuỗi hiển thị
     */
    static String formatPoints(int points) {
        if (points >= 1000000) {
            return String.format("%.1fM", points / 1000000.0);
        } else if (points >= 1000) {
            return String.format("%.1fK", points / 1000.0);
        } else {
            return String.valueOf(points);
        }
    }
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)      // Tăng timeout kết nối
            .readTimeout(120, TimeUnit.SECONDS)        // Tăng timeout đọc dữ liệu
            .writeTimeout(120, TimeUnit.SECONDS)       // Tăng timeout ghi dữ liệu
            .callTimeout(180, TimeUnit.SECONDS)        // Tăng timeout tổng thể
            .addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true"); // Cho ngrok
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            })
            .build();

    // ===== SINGLETON INSTANCE =====
    SimpleOrderApiService orderApiService = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Sử dụng custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SimpleOrderApiService.class);
}