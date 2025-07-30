package com.android.projectandroid.data.userModel;

import androidx.annotation.NonNull;

import com.android.projectandroid.data.productModel.CreateProductResponse;
import com.android.projectandroid.data.productModel.GetProductsResponse;
import com.android.projectandroid.data.productModel.Product;
import com.android.projectandroid.data.productModel.ProductDeserializer;
import com.android.projectandroid.data.productModel.UpdateProductStatusRequest;
import com.android.projectandroid.data.productModel.UpdateProductStatusResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Create Gson with custom deserializer
    Gson gson = new GsonBuilder()
            .setDateFormat("dd-MM-yyyy HH:mm:ss")
            .registerTypeAdapter(Product.class, new ProductDeserializer())
            .create();

    // Create OkHttpClient with increased timeout for file uploads
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

    ApiService apiService = new Retrofit.Builder()
            .baseUrl("https://modern-outgoing-quail.ngrok-free.app/api/")
            .client(okHttpClient)  // Sử dụng custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @POST("users/login")
    Call<NormalLoginResponse> login(@Body NormalLoginRequest normalLoginRequest);

    @POST("users/google-sign-in")
    Call<LoginWithGoogleResponse> google_sign_in(@Body LoginWithGoogleRequest loginWithGoogleRequest);

    @POST("users/register")
    Call<NormalLoginResponse> sign_up(@Body SignUpRequest signUpRequest );

    @POST("users/forgot-password")
    Call<ForgotPasswordResponse> forgot_password(@Body ForgotPasswordRequest forgotPasswordRequest );

    @POST("users/verify")
    Call<ForgotPasswordResponse> verify(@Body VerificationRequest verificationRequest);

    @PUT("users/edit")
    Call<ForgotPasswordResponse> update_password(@Header("Authorization") String token,@Body UpdatePasswordRequest updatePasswordRequest );
    @PUT("users/edit")
    Call<UpdateProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @PUT("users/edit")
    Call<AddPointsResponse> updatepoint(@Body AddPointsRequest request);

    @Multipart
    @POST("users/edit")
    Call<UpdateProfileResponse> uploadAvatar(
            @Part("userId") RequestBody userId,
            @Part("type") RequestBody type,
            @Part MultipartBody.Part image
    );


    // ✅ ĐỔI MẬT KHẨU KHÔNG CẦN TOKEN
    @PUT("users/edit")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    // ✅ LẤY THÔNG TIN USER THEO ID
    @GET("users/{userId}")
    Call<UserInfoResponse> getUserById(@Path("userId") String userId);

    // API tạo sản phẩm pass đồ với upload nhiều ảnh - Tăng timeout
    @Multipart
    @POST("pass-products")
    Call<CreateProductResponse> createPassProduct(
            @Part("productName") RequestBody productName,
            @Part("category") RequestBody category,
            @Part("originalPrice") RequestBody originalPrice,
            @Part("condition") RequestBody condition,
            @Part("purchasePrice") RequestBody purchasePrice,
            @Part("shortDescription") RequestBody shortDescription,
            @Part("detailedDescription") RequestBody detailedDescription,
            @Part("userId") RequestBody userId,
            @Part List<MultipartBody.Part> images
    );

    // API lấy sản phẩm theo user
    @GET("pass-products/user/{userId}")
    Call<GetProductsResponse> getUserProducts(@Path("userId") String userId);

    // API lấy tất cả sản phẩm
    @GET("pass-products")
    Call<GetProductsResponse> getAllProducts(@Query("status") String status);

    // API cập nhật trạng thái sản phẩm (mua hàng)
    @PUT("pass-products/{productId}/status")
    Call<UpdateProductStatusResponse> updateProductStatus(
            @Path("productId") String productId,
            @Body UpdateProductStatusRequest request
    );
}