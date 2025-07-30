package com.android.projectandroid.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.projectandroid.R;
import com.android.projectandroid.data.orderModel.CreateOrderRequest;
import com.android.projectandroid.data.orderModel.CreateOrderResponse;
import com.android.projectandroid.data.orderModel.SimpleOrderApiService;
import com.android.projectandroid.data.productModel.ProductRepository;
import com.android.projectandroid.data.productModel.CreateProductResponse;
import com.android.projectandroid.data.userModel.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassDo extends Fragment {

    private EditText etProductName, etPrice, etShortDescription, etDetailedDescription;
    private Spinner spinnerCondition , spinnerCategory;
    private TextView tvPurchasePrice;
    private FrameLayout imageUpload1, imageUpload2;
    private ImageView selectedImageView1, selectedImageView2;
    private TextView plusText1, plusText2;
    private TextView btnSubmit;
    PreferenceManager preferenceManager;

    // ActivityResultLauncher cho việc chọn ảnh
    private ActivityResultLauncher<Intent> imagePickerLauncher1;
    private ActivityResultLauncher<Intent> imagePickerLauncher2;

    // Danh sách Uri của các ảnh đã chọn
    private List<Uri> selectedImageUris;
    private ProductRepository productRepository;

    // Hệ số tình trạng sản phẩm theo string array
    private final double[] conditionMultipliers = {
            1.0,    // Mới 100% (chưa sử dụng)
            0.99,   // Như mới 99% (sử dụng 1-2 lần)
            0.95,   // Rất tốt 95% (ít dấu hiệu sử dụng)
            0.90,   // Tốt 90% (có dấu hiệu sử dụng nhẹ)
            0.85,   // Khá tốt 85% (sử dụng bình thường)
            0.80,   // Trung bình 80% (có dấu hiệu sử dụng rõ)
            0.70    // Còn dùng được 70% (nhiều dấu hiệu sử dụng)
    };

    private final String[] conditionStrings = {
            "Mới 100% (chưa sử dụng)",
            "Như mới 99% (sử dụng 1-2 lần)",
            "Rất tốt 95% (ít dấu hiệu sử dụng)",
            "Tốt 90% (có dấu hiệu sử dụng nhẹ)",
            "Khá tốt 85% (sử dụng bình thường)",
            "Trung bình 80% (có dấu hiệu sử dụng rõ)",
            "Còn dùng được 70% (nhiều dấu hiệu sử dụng)"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(getActivity());
        super.onCreate(savedInstanceState);

        selectedImageUris = new ArrayList<>();
        productRepository = new ProductRepository();

        preferenceManager = new PreferenceManager(getContext());

        setupImagePickerLaunchers();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_passdo, container, false);

        // Đảm bảo padding cho view theo hệ thống (status bar, nav bar) động
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.pass_do), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Khởi tạo các view
        initViews(view);

        // Thiết lập các listener
        setupListeners();

        return view;
    }

    private void setupImagePickerLaunchers() {
        // Khởi tạo ActivityResultLauncher cho ảnh 1
        imagePickerLauncher1 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            setImageToFrameLayout(imageUri, 1);
                        }
                    }
                }
        );

        // Khởi tạo ActivityResultLauncher cho ảnh 2
        imagePickerLauncher2 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            setImageToFrameLayout(imageUri, 2);
                        }
                    }
                }
        );
    }

    private void initViews(View view) {
        etProductName = view.findViewById(R.id.et_product_name);
        etPrice = view.findViewById(R.id.et_price);
        etShortDescription = view.findViewById(R.id.et_short_description);
        etDetailedDescription = view.findViewById(R.id.et_detailed_description);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        tvPurchasePrice = view.findViewById(R.id.giathumua);
        btnSubmit = view.findViewById(R.id.btn_submit);
//        progressBar = view.findViewById(R.id.progress_bar);

        // Khởi tạo các FrameLayout cho upload ảnh
        imageUpload1 = view.findViewById(R.id.image_upload_1);
        imageUpload2 = view.findViewById(R.id.image_upload_2);

        setupImageViews();
    }

    private void setupImageViews() {
        // Setup cho ảnh 1
        plusText1 = (TextView) imageUpload1.getChildAt(0);
        selectedImageView1 = new ImageView(getContext());
        selectedImageView1.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        selectedImageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);
        selectedImageView1.setVisibility(View.GONE);
        imageUpload1.addView(selectedImageView1);

        // Setup cho ảnh 2
        plusText2 = (TextView) imageUpload2.getChildAt(0);
        selectedImageView2 = new ImageView(getContext());
        selectedImageView2.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        selectedImageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        selectedImageView2.setVisibility(View.GONE);
        imageUpload2.addView(selectedImageView2);
    }

    private void setupListeners() {
        // Listener cho EditText giá gốc
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculatePurchasePriceLocal();
            }
        });

        // Listener cho Spinner tình trạng
        spinnerCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculatePurchasePriceLocal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listener cho các FrameLayout upload ảnh
        imageUpload1.setOnClickListener(v -> openImagePicker(1));
        imageUpload2.setOnClickListener(v -> openImagePicker(2));

        // Listener cho button submit
        btnSubmit.setOnClickListener(v -> submitProduct());
    }

    private void calculatePurchasePriceLocal() {
        try {
            String priceText = etPrice.getText().toString().trim();

            if (priceText.isEmpty()) {
                tvPurchasePrice.setText("");
                return;
            }

            double originalPrice = Double.parseDouble(priceText);
            int conditionPosition = spinnerCondition.getSelectedItemPosition();

            if (conditionPosition < 0 || conditionPosition >= conditionMultipliers.length) {
                tvPurchasePrice.setText("");
                return;
            }

            double purchasePrice = originalPrice * 0.6 * conditionMultipliers[conditionPosition];

            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedPrice = formatter.format(purchasePrice);

            tvPurchasePrice.setText("Giá thu mua: " + formattedPrice + " VNĐ");

        } catch (NumberFormatException e) {
            tvPurchasePrice.setText("");
        }
    }


    private void submitProduct() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Show progress
        btnSubmit.setEnabled(false);

        // Get form data
        String productName = etProductName.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString().trim();
        double originalPrice = Double.parseDouble(etPrice.getText().toString().trim());
        String condition = conditionStrings[spinnerCondition.getSelectedItemPosition()];
        String shortDescription = etShortDescription.getText().toString().trim();
        String detailedDescription = etDetailedDescription.getText().toString().trim();

        // Calculate purchase price
        double purchasePrice = originalPrice * 0.6 * conditionMultipliers[spinnerCondition.getSelectedItemPosition()];

        // Get userId from SharedPreferences or user session
        String userId = getUserId();
        String token = getAuthToken();

        // Call API to create product
        productRepository.createProduct(
                productName,
                category,
                originalPrice,
                condition,
                purchasePrice,
                shortDescription,
                detailedDescription,
                userId,
                selectedImageUris,
                getContext(),
                new ProductRepository.ProductCallback() {
                    @Override
                    public void onSuccess(CreateProductResponse response) {
                        if (response.isSuccess()) {
                            // ✅ TẠO ĐƠN HÀNG SAU KHI PRODUCT THÀNH CÔNG
                            String productId = response.getData().getId();
                            Double price = purchasePrice;
                            Log.d("PassDo", "Product created successfully with ID: " + price);

                            // ✅ GỌI PLACE ORDER VỚI XỬ LÝ RESPONSE
                            placeOrder("pass_pickup", productId ,price);

                        } else {
                            btnSubmit.setEnabled(true);
                            Toast.makeText(getContext(), "Lỗi tạo sản phẩm: " + response.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        btnSubmit.setEnabled(true);
                        Log.e("PassDo", "Product creation error: " + error);
                        Toast.makeText(getContext(), "Lỗi kết nối: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void placeOrder(String orderType, String productId, Double price) {
        String userId = preferenceManager.getUserId();
        String kg = "0"; // ✅ CHO PASS_PICKUP THÌ KG = "0" HOẶC NULL
        String notes = "Đơn hàng Pass đồ";

        Log.d("PassDo", "Placing order - userId: " + userId + ", productId: " + productId + ", orderType: " + orderType +", Price:" + price);

        // ✅ KIỂM TRA USERID TRƯỚC KHI TẠO ORDER
        if (userId == null || userId.isEmpty()) {
            Log.e("PassDo", "User ID is null or empty");
            btnSubmit.setEnabled(true);
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_LONG).show();
            return;
        }


        CreateOrderRequest request = new CreateOrderRequest(userId, productId, orderType, notes, kg ,price);

        SimpleOrderApiService.orderApiService.createOrder(request)
                .enqueue(new Callback<CreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                        btnSubmit.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            CreateOrderResponse orderResponse = response.body();

                            if (orderResponse.isSuccess()) {
                                Log.d("PassDo", "Order created successfully: " + orderResponse.getData());
                                Toast.makeText(getContext(),
                                        "Sản phẩm và đơn hàng đã được tạo thành công!",
                                        Toast.LENGTH_LONG).show();
                                clearForm();
                            } else {
                                Log.e("PassDo", "Order creation failed: " + orderResponse.getMessage());
                                Toast.makeText(getContext(),
                                        "Sản phẩm đã tạo nhưng lỗi tạo đơn hàng: " + orderResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.e("PassDo", "Order API response not successful. Code: " + response.code());
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                Log.e("PassDo", "Error body: " + errorBody);
                                Toast.makeText(getContext(),
                                        "Sản phẩm đã tạo nhưng lỗi tạo đơn hàng: " + errorBody,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.e("PassDo", "Error reading error body", e);
                                Toast.makeText(getContext(),
                                        "Sản phẩm đã tạo nhưng lỗi tạo đơn hàng: HTTP " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call, Throwable throwable) {
                        btnSubmit.setEnabled(true);
                        Log.e("PassDo", "Order API call failed", throwable);
                        Toast.makeText(getContext(),
                                "Sản phẩm đã tạo nhưng lỗi kết nối khi tạo đơn hàng: " + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput() {
        // Validate product name
        if (etProductName.getText().toString().trim().isEmpty()) {
            etProductName.setError("Vui lòng nhập tên sản phẩm");
            return false;
        }

        // Validate price
        String priceText = etPrice.getText().toString().trim();
        if (priceText.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá gốc");
            return false;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                etPrice.setError("Giá phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Giá không hợp lệ");
            return false;
        }

        // Validate condition
        if (spinnerCondition.getSelectedItemPosition() < 0) {
            Toast.makeText(getContext(), "Vui lòng chọn tình trạng sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate short description
        if (etShortDescription.getText().toString().trim().isEmpty()) {
            etShortDescription.setError("Vui lòng nhập mô tả ngắn");
            return false;
        }

        // Validate detailed description
        if (etDetailedDescription.getText().toString().trim().isEmpty()) {
            etDetailedDescription.setError("Vui lòng nhập mô tả chi tiết");
            return false;
        }

        // Validate images
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etProductName.setText("");
        etPrice.setText("");
        etShortDescription.setText("");
        etDetailedDescription.setText("");
        spinnerCondition.setSelection(0);
        tvPurchasePrice.setText("");

        // Clear images
        selectedImageUris.clear();
        clearAllImages();
    }

    private void clearAllImages() {
        // Reset image 1
        if (selectedImageView1 != null) {
            selectedImageView1.setVisibility(View.GONE);
            plusText1.setVisibility(View.VISIBLE);
        }

        // Reset image 2
        if (selectedImageView2 != null) {
            selectedImageView2.setVisibility(View.GONE);
            plusText2.setVisibility(View.VISIBLE);
        }
    }

    // Phương thức mở image picker
    private void openImagePicker(int imageNumber) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        switch (imageNumber) {
            case 1:
                imagePickerLauncher1.launch(intent);
                break;
            case 2:
                imagePickerLauncher2.launch(intent);
                break;
        }
    }

    // Thêm các methods này vào class PassDo

    /**
     * Nén ảnh để giảm kích thước file
     */
    private Uri compressImage(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) return imageUri;

            // Xoay ảnh nếu cần thiết
            Bitmap rotatedBitmap = rotateImageIfRequired(originalBitmap, imageUri);

            // Resize ảnh nếu quá lớn
            Bitmap resizedBitmap = resizeImage(rotatedBitmap, 1200, 1200);

            // Nén ảnh
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            // Lưu ảnh đã nén vào file tạm
            String fileName = "compressed_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fileOutputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.close();
            outputStream.close();

            // Dọn dẹp bitmap
            if (rotatedBitmap != originalBitmap) rotatedBitmap.recycle();
            if (resizedBitmap != rotatedBitmap) resizedBitmap.recycle();
            originalBitmap.recycle();

            // Trả về Uri của file đã nén
            return Uri.fromFile(new java.io.File(getContext().getFilesDir(), fileName));

        } catch (Exception e) {
            Log.e("PassDo", "Error compressing image: " + e.getMessage());
            return imageUri; // Trả về ảnh gốc nếu nén thất bại
        }
    }

    /**
     * Xoay ảnh theo thông tin EXIF
     */
    private Bitmap rotateImageIfRequired(Bitmap bitmap, Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            inputStream.close();

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            return bitmap;
        }
    }

    /**
     * Xoay ảnh theo góc
     */
    private Bitmap rotateImage(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Resize ảnh theo kích thước tối đa
     */
    private Bitmap resizeImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scaleWidth = (float) maxWidth / width;
        float scaleHeight = (float) maxHeight / height;
        float scale = Math.min(scaleWidth, scaleHeight);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Cập nhật method setImageToFrameLayout để nén ảnh
     */
    private void setImageToFrameLayout(Uri imageUri, int imageNumber) {
        // Nén ảnh trước khi lưu
        Uri compressedUri = compressImage(imageUri);

        // Thêm hoặc thay thế Uri trong list
        if (imageNumber <= selectedImageUris.size()) {
            if (imageNumber == selectedImageUris.size() + 1) {
                selectedImageUris.add(compressedUri);
            } else {
                selectedImageUris.set(imageNumber - 1, compressedUri);
            }
        } else {
            selectedImageUris.add(compressedUri);
        }

        // Hiển thị ảnh trong UI (sử dụng ảnh gốc để hiển thị cho chất lượng tốt hơn)
        switch (imageNumber) {
            case 1:
                selectedImageView1.setImageURI(imageUri);
                selectedImageView1.setVisibility(View.VISIBLE);
                plusText1.setVisibility(View.GONE);
                break;
            case 2:
                selectedImageView2.setImageURI(imageUri);
                selectedImageView2.setVisibility(View.VISIBLE);
                plusText2.setVisibility(View.GONE);
                break;
        }
    }

    // Implement these methods based on your app's user management
// Complete the getUserId() method
    private String getUserId() {
        if (preferenceManager != null) {
            return preferenceManager.getUserId();
        }
        return "";
    }

    // Complete the getAuthToken() method
    private String getAuthToken() {
        if (preferenceManager != null) {
            return preferenceManager.getAuthToken();
        }
        return "";
    }
}