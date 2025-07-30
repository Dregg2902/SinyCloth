package com.android.projectandroid.data.productModel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.projectandroid.data.userModel.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private ApiService apiService;

    public ProductRepository() {
        this.apiService = ApiService.apiService;
    }

    // Tạo sản phẩm mới
    public void createProduct(String productName, String category,
                              double originalPrice, String condition, double purchasePrice,
                              String shortDescription, String detailedDescription,
                              String userId, List<Uri> imageUris, Context context,
                              ProductCallback callback) {

        // Tạo RequestBody cho các trường text
        RequestBody productNameBody = ImageUploadHelper.createPartFromString(productName);
        RequestBody categoryBody = ImageUploadHelper.createPartFromString(category);
        RequestBody originalPriceBody = ImageUploadHelper.createPartFromString(String.valueOf(originalPrice));
        RequestBody conditionBody = ImageUploadHelper.createPartFromString(condition);
        RequestBody purchasePriceBody = ImageUploadHelper.createPartFromString(String.valueOf(purchasePrice));
        RequestBody shortDescBody = ImageUploadHelper.createPartFromString(shortDescription);
        RequestBody detailedDescBody = ImageUploadHelper.createPartFromString(detailedDescription);
        RequestBody userIdBody = ImageUploadHelper.createPartFromString(userId);

        // Tạo list MultipartBody.Part cho ảnh
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri imageUri : imageUris) {
            MultipartBody.Part part = ImageUploadHelper.createImagePart(context, imageUri, "images");
            if (part != null) {
                imageParts.add(part);
            }
        }

        // Thực hiện API call
        Call<CreateProductResponse> call = apiService.createPassProduct(
//                "Bearer " + token,
                productNameBody,
                categoryBody,
                originalPriceBody,
                conditionBody,
                purchasePriceBody,
                shortDescBody,
                detailedDescBody,
                userIdBody,
                imageParts
        );

        call.enqueue(new Callback<CreateProductResponse>() {
            @Override
            public void onResponse(Call<CreateProductResponse> call, Response<CreateProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("PassDo", "API error code: " + response.code() + ", error body: " + errorBody);
                    callback.onError("Failed to create product: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CreateProductResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Callback interfaces
    public interface ProductCallback {
        void onSuccess(CreateProductResponse response);
        void onError(String error);
    }
}
