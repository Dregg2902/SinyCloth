package com.android.projectandroid.data.userModel;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utility class để upload ảnh lên Cloudinary sử dụng unsigned upload
 */
public class CloudinaryUploader {

    // ✅ THAY ĐỔI CÁC GIÁ TRỊ NÀY THEO CLOUDINARY ACCOUNT CỦA BẠN
    private static final String CLOUD_NAME = "your_cloud_name";
    private static final String UPLOAD_PRESET = "your_upload_preset"; // Tạo unsigned upload preset trong Cloudinary

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
    private static final String TAG = "CloudinaryUploader";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    /**
     * Upload ảnh lên Cloudinary using unsigned upload preset
     * @param imageData byte array của ảnh
     * @param callback callback để xử lý kết quả
     */
    public static void uploadImage(byte[] imageData, UploadCallback callback) {
        // Convert byte array to base64
        String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);
        String dataUrl = "data:image/jpeg;base64," + base64Image;

        // Tạo request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", dataUrl)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", "user_avatars") // Tùy chọn: tạo folder riêng
                .addFormDataPart("resource_type", "image")
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        // Thực hiện upload bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Upload failed", e);
                if (callback != null) {
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Upload response: " + responseBody);

                    if (response.isSuccessful()) {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String secureUrl = jsonResponse.getString("secure_url");

                        Log.d(TAG, "Upload successful: " + secureUrl);
                        if (callback != null) {
                            callback.onSuccess(secureUrl);
                        }
                    } else {
                        Log.e(TAG, "Upload failed with code: " + response.code());
                        String errorMessage = "Upload failed";

                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("error")) {
                                JSONObject error = errorJson.getJSONObject("error");
                                errorMessage = error.getString("message");
                            }
                        } catch (Exception e) {
                            errorMessage = "HTTP " + response.code();
                        }

                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                    if (callback != null) {
                        callback.onError("Lỗi xử lý phản hồi: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Upload ảnh với các options tùy chỉnh
     */
    public static void uploadImageWithOptions(byte[] imageData, String folder,
                                              int quality, UploadCallback callback) {
        String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);
        String dataUrl = "data:image/jpeg;base64," + base64Image;

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", dataUrl)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("resource_type", "image");

        // Thêm folder nếu có
        if (folder != null && !folder.isEmpty()) {
            builder.addFormDataPart("folder", folder);
        }

        // Thêm quality transformation
        if (quality > 0 && quality <= 100) {
            builder.addFormDataPart("quality", String.valueOf(quality));
        }

        // Thêm transformation để resize ảnh (tùy chọn)
        builder.addFormDataPart("transformation", "c_fill,w_400,h_400,q_auto");

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Upload failed", e);
                if (callback != null) {
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String secureUrl = jsonResponse.getString("secure_url");

                        if (callback != null) {
                            callback.onSuccess(secureUrl);
                        }
                    } else {
                        String errorMessage = "Upload failed";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("error")) {
                                JSONObject error = errorJson.getJSONObject("error");
                                errorMessage = error.getString("message");
                            }
                        } catch (Exception e) {
                            errorMessage = "HTTP " + response.code();
                        }

                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError("Lỗi xử lý phản hồi: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Kiểm tra config Cloudinary
     */
    public static boolean isConfigured() {
        return !CLOUD_NAME.equals("your_cloud_name") &&
                !UPLOAD_PRESET.equals("your_upload_preset");
    }

    /**
     * Upload avatar với settings tối ưu
     */
    public static void uploadAvatar(byte[] imageData, UploadCallback callback) {
        if (!isConfigured()) {
            if (callback != null) {
                callback.onError("Cloudinary chưa được cấu hình. Vui lòng cập nhật CLOUD_NAME và UPLOAD_PRESET");
            }
            return;
        }

        uploadImageWithOptions(imageData, "user_avatars", 80, callback);
    }
}