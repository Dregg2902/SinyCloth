package com.android.projectandroid.user;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.data.userModel.UpdateProfileRequest;
import com.android.projectandroid.data.userModel.UpdateProfileResponse;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.ChangePasswordRequest;
import com.android.projectandroid.data.userModel.ChangePasswordResponse;
import com.android.projectandroid.data.productModel.ImageUploadHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity quản lý thông tin người dùng với chế độ View/Edit
 */
public class UserProfileActivity extends AppCompatActivity {

    // UI Components - Common
    private ImageView ivAvatar, ivCameraIcon;
    private TextView tvUsername, tvEmail, tvPoints, tvLevel, tvProfileCompletion;
    private ImageButton btnEdit ,btnback;
    private CardView cardUserStats;

    // UI Components - View Mode
    private TextView tvFullNameView, tvPhoneNumberView, tvDateOfBirthView, tvGenderView, tvAddressView, tvFullName;

    // UI Components - Edit Mode
    private EditText etFullName, etPhoneNumber, etAddress, etDateOfBirth;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private LinearLayout layoutActionButtons;
    private Button btnSave, btnCancel, btnChangePassword;

    // State Management
    private boolean isEditMode = false;
    private NormalUser currentUser;
    private PreferenceManager preferenceManager;
    private String avatarUrl = null;
    private boolean isAvatarChanged = false;
    private Uri selectedImageUri = null; // Store selected image URI for multipart upload

    // Original data for cancel functionality
    private String originalFullName, originalPhoneNumber, originalAddress, originalDateOfBirth, originalGender;

    // Image handling
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        initImageHandlers();
        loadUserData();
        setupListeners();
        setViewMode(); // Bắt đầu ở chế độ xem

        setTitle("👤 Thông tin cá nhân");
    }

    private void initViews() {
        // Common components
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCameraIcon = findViewById(R.id.ivCameraIcon);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPoints = findViewById(R.id.tvPoints);
        tvLevel = findViewById(R.id.tvLevel);
        tvProfileCompletion = findViewById(R.id.tvProfileCompletion);
        btnEdit = findViewById(R.id.btnEdit);
        btnback = findViewById(R.id.btnBack2);
        cardUserStats = findViewById(R.id.cardUserStats);

        // View Mode components
        tvFullNameView = findViewById(R.id.tvFullNameView);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhoneNumberView = findViewById(R.id.tvPhoneNumberView);
        tvDateOfBirthView = findViewById(R.id.tvDateOfBirthView);
        tvGenderView = findViewById(R.id.tvGenderView);
        tvAddressView = findViewById(R.id.tvAddressView);

        // Edit Mode components
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etAddress = findViewById(R.id.etAddress);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Setup date picker for date of birth
        etDateOfBirth.setInputType(InputType.TYPE_NULL);
        etDateOfBirth.setFocusable(false);
    }

    private void initImageHandlers() {
        // Image picker from gallery
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                processSelectedImage(bitmap, imageUri);
                            } catch (IOException e) {
                                showError("Lỗi khi tải ảnh: " + e.getMessage());
                            }
                        }
                    }
                });

        // Camera capture - Modified to save to URI
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap != null) {
                                // Convert bitmap to URI for multipart upload
                                Uri imageUri = saveBitmapToUri(bitmap);
                                if (imageUri != null) {
                                    processSelectedImage(bitmap, imageUri);
                                } else {
                                    showError("Không thể lưu ảnh từ camera");
                                }
                            }
                        }
                    }
                });

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        showError("Cần cấp quyền camera để chụp ảnh");
                    }
                });
    }

    private void setupListeners() {
        // Edit/Cancel buttons
        btnEdit.setOnClickListener(v -> {
            if (isEditMode) {
                cancelEdit();
            } else {
                setEditMode();
            }
        });
        btnback.setOnClickListener(v -> Back());

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> cancelEdit());

        // Avatar upload (only in edit mode)
        ivCameraIcon.setOnClickListener(v -> {
            if (isEditMode) {
                showImagePickerDialog();
            }
        });

        // Date picker
        etDateOfBirth.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePicker();
            }
        });

        // Change password
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Avatar preview
        ivAvatar.setOnClickListener(v -> {
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                showAvatarPreview();
            }
        });
    }

    private void Back() {
        super.onBackPressed();
        finish();
    }

    /**
     * Convert bitmap to URI for multipart upload
     */
    private Uri saveBitmapToUri(Bitmap bitmap) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Avatar", null);
            return Uri.parse(path);
        } catch (Exception e) {
            Log.e("UserProfile", "Error saving bitmap to URI", e);
            return null;
        }
    }

    /**
     * Chuyển sang chế độ xem (View Mode)
     */
    private void setViewMode() {
        isEditMode = false;

        // Hiển thị View Mode components
        tvFullNameView.setVisibility(View.VISIBLE);
        tvPhoneNumberView.setVisibility(View.VISIBLE);
        tvDateOfBirthView.setVisibility(View.VISIBLE);
        tvGenderView.setVisibility(View.VISIBLE);
        tvAddressView.setVisibility(View.VISIBLE);

        // Ẩn Edit Mode components
        etFullName.setVisibility(View.GONE);
        etPhoneNumber.setVisibility(View.GONE);
        etAddress.setVisibility(View.GONE);
        etDateOfBirth.setVisibility(View.GONE);
        rgGender.setVisibility(View.GONE);
        layoutActionButtons.setVisibility(View.GONE);
        ivCameraIcon.setVisibility(View.GONE);

        // Thay đổi icon edit button
        btnEdit.setImageResource(R.drawable.ic_edit);

        // Hiển thị data trong View Mode
        displayUserDataInViewMode();

        Log.d("UserProfile", "Switched to View Mode");
    }

    /**
     * Chuyển sang chế độ chỉnh sửa (Edit Mode)
     */
    private void setEditMode() {
        isEditMode = true;

        // Lưu data gốc để có thể cancel
        saveOriginalData();

        // Ẩn View Mode components
        tvFullNameView.setVisibility(View.GONE);
        tvPhoneNumberView.setVisibility(View.GONE);
        tvDateOfBirthView.setVisibility(View.GONE);
        tvGenderView.setVisibility(View.GONE);
        tvAddressView.setVisibility(View.GONE);

        // Hiển thị Edit Mode components
        etFullName.setVisibility(View.VISIBLE);
        etPhoneNumber.setVisibility(View.VISIBLE);
        etAddress.setVisibility(View.VISIBLE);
        etDateOfBirth.setVisibility(View.VISIBLE);
        rgGender.setVisibility(View.VISIBLE);
        layoutActionButtons.setVisibility(View.VISIBLE);
        ivCameraIcon.setVisibility(View.VISIBLE);

        // Thay đổi icon edit button thành close
        btnEdit.setImageResource(R.drawable.ic_close);

        // Điền data vào Edit Mode
        populateEditMode();

        Log.d("UserProfile", "Switched to Edit Mode");
    }

    /**
     * Hủy chỉnh sửa và quay về View Mode
     */
    private void cancelEdit() {
        // Khôi phục data gốc
        restoreOriginalData();

        // Reset avatar nếu có thay đổi
        if (isAvatarChanged) {
            loadAvatar();
            isAvatarChanged = false;
            selectedImageUri = null;
        }

        // Chuyển về View Mode
        setViewMode();

        Toast.makeText(this, "Đã hủy chỉnh sửa", Toast.LENGTH_SHORT).show();
    }

    /**
     * Hiển thị dữ liệu người dùng trong chế độ xem
     */
    private void displayUserDataInViewMode() {
        if (currentUser == null) return;

        tvFullNameView.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "Chưa cập nhật");
        tvPhoneNumberView.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Chưa cập nhật");
        tvDateOfBirthView.setText(currentUser.getDateOfBirth() != null ? currentUser.getDateOfBirth() : "Chưa cập nhật");
        tvGenderView.setText(getGenderDisplayText(currentUser.getGender()));
        tvAddressView.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "Chưa cập nhật");
    }

    /**
     * Điền dữ liệu vào các trường trong chế độ chỉnh sửa
     */
    private void populateEditMode() {
        if (currentUser == null) return;

        etFullName.setText(currentUser.getFullName());
        etPhoneNumber.setText(currentUser.getPhoneNumber());
        etAddress.setText(currentUser.getAddress());
        etDateOfBirth.setText(currentUser.getDateOfBirth());

        // Set gender selection
        setGenderSelection(currentUser.getGender());
    }

    /**
     * Lưu dữ liệu gốc để có thể cancel
     */
    // Sửa phương thức saveOriginalData()
    private void saveOriginalData() {
        if (currentUser != null) {
            originalFullName = currentUser.getFullName(); // ✅ Sửa lỗi này
            originalPhoneNumber = currentUser.getPhoneNumber();
            originalAddress = currentUser.getAddress();
            originalDateOfBirth = currentUser.getDateOfBirth();
            originalGender = currentUser.getGender();
        }
    }

    // Sửa phương thức restoreOriginalData()
    private void restoreOriginalData() {
        if (currentUser != null) {
            currentUser.setFullName(originalFullName); // ✅ Sửa lỗi này
            currentUser.setPhoneNumber(originalPhoneNumber);
            currentUser.setAddress(originalAddress);
            currentUser.setDateOfBirth(originalDateOfBirth);
            currentUser.setGender(originalGender);
        }
    }


    /**
     * Chuyển đổi gender code thành text hiển thị
     */
    private String getGenderDisplayText(String gender) {
        if (gender == null) return "Chưa cập nhật";

        switch (gender.toLowerCase()) {
            case "male":
                return "Nam";
            case "female":
                return "Nữ";
            case "other":
                return "Khác";
            default:
                return "Chưa cập nhật";
        }
    }

    private void loadUserData() {
        preferenceManager = new PreferenceManager(this);
        currentUser = preferenceManager.getCurrentUser();

        if (currentUser == null) {
            showError("Không tìm thấy thông tin người dùng");
            finish();
            return;
        }

        // Hiển thị thông tin cơ bản (luôn hiển thị)
        tvFullName.setText(currentUser.getFullName());
        tvFullNameView.setText(currentUser.getFullName());
        tvUsername.setText("@" + currentUser.getUsername());
        tvEmail.setText(currentUser.getEmail());
        tvPoints.setText(currentUser.getFormattedPoints());
        tvLevel.setText(currentUser.getPointLevel());
        tvLevel.setTextColor(currentUser.getPointLevelColor());

        // Tính phần trăm hoàn thành profile
        calculateProfileCompletion();

        // Load avatar
        loadAvatar();

        Log.d("UserProfile", "User loaded: " + currentUser.toString());
    }

    /**
     * Tính phần trăm hoàn thành profile
     */
    private void calculateProfileCompletion() {
        int completedFields = 0;
        int totalFields = 5; // username, phone, address, dateOfBirth, gender

        if (currentUser.getUsername() != null && !currentUser.getUsername().isEmpty()) completedFields++;
        if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty()) completedFields++;
        if (currentUser.getAddress() != null && !currentUser.getAddress().isEmpty()) completedFields++;
        if (currentUser.getDateOfBirth() != null && !currentUser.getDateOfBirth().isEmpty()) completedFields++;
        if (currentUser.getGender() != null && !currentUser.getGender().isEmpty()) completedFields++;

        int percentage = (completedFields * 100) / totalFields;
        tvProfileCompletion.setText(percentage + "%");

        // Đổi màu theo mức độ hoàn thành
        if (percentage >= 80) {
            tvProfileCompletion.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else if (percentage >= 50) {
            tvProfileCompletion.setTextColor(ContextCompat.getColor(this, R.color.warning));
        } else {
            tvProfileCompletion.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    private void setGenderSelection(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case "male":
                    rbMale.setChecked(true);
                    break;
                case "female":
                    rbFemale.setChecked(true);
                    break;
                case "other":
                    rbOther.setChecked(true);
                    break;
            }
        }
    }

    private void loadAvatar() {
        Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.avatarshipper)
                        .error(R.drawable.shippersetting)
                        .circleCrop())
                .into(ivAvatar);
    }

    private void showImagePickerDialog() {
        if (!isEditMode) return;

        String[] options = {"📷 Chụp ảnh", "🖼️ Chọn từ thư viện", "❌ Hủy"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndOpen();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            showError("Không tìm thấy ứng dụng camera");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void processSelectedImage(Bitmap bitmap, Uri imageUri) {
        if (!isEditMode) return;

        // Hiển thị ảnh preview
        ivAvatar.setImageBitmap(bitmap);
        isAvatarChanged = true;
        selectedImageUri = imageUri;

        Toast.makeText(this, "✅ Đã chọn ảnh mới", Toast.LENGTH_SHORT).show();

        Log.d("UserProfile", "Image selected for upload: " + imageUri.toString());
    }

    /**
     * Upload avatar using multipart
     */
    private void uploadAvatarMultipart() {
        if (selectedImageUri == null) {
            Log.d("UserProfile", "No image selected for upload");
            return;
        }

        try {
            // Create multipart using ImageUploadHelper
            MultipartBody.Part imagePart = ImageUploadHelper.createImagePart(this, selectedImageUri, "image");

            if (imagePart == null) {
                showError("Không thể tạo file upload");
                return;
            }

            // Create other parts
            RequestBody userIdPart = ImageUploadHelper.createPartFromString(currentUser.get_id());
            RequestBody typePart = ImageUploadHelper.createPartFromString("upload_avatar");

            Log.d("UserProfile", "Starting avatar upload for user: " + currentUser.get_id());

            // Call API
            Call<UpdateProfileResponse> call = ApiService.apiService.uploadAvatar(
                    userIdPart,
                    typePart,
                    imagePart
            );

            call.enqueue(new Callback<UpdateProfileResponse>() {
                @Override
                public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UpdateProfileResponse uploadResponse = response.body();
                        if (uploadResponse.isSuccess()) {
                            // Update avatar URL from response
                            if (uploadResponse.getUserInfo() != null && uploadResponse.getUserInfo().getAvatarUrl() != null) {
                                avatarUrl = uploadResponse.getUserInfo().getAvatarUrl();
                                currentUser.setAvatarUrl(avatarUrl);

                                // Update local storage
                                preferenceManager.updateNormalUser(currentUser);

                                Toast.makeText(UserProfileActivity.this,
                                        "✅ Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();

                                Log.d("UserProfile", "Avatar uploaded successfully: " + avatarUrl);
                            }
                        } else {
                            showError("Lỗi upload ảnh: " + uploadResponse.getError());
                        }
                    } else {
                        showError("Không thể upload ảnh đại diện");
                        Log.e("UserProfile", "Upload failed with response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                    showError("Lỗi kết nối khi upload ảnh: " + t.getMessage());
                    Log.e("UserProfile", "Avatar upload failed", t);
                }
            });

        } catch (Exception e) {
            showError("Lỗi khi chuẩn bị upload ảnh: " + e.getMessage());
            Log.e("UserProfile", "Error preparing avatar upload", e);
        }
    }

    private void showDatePicker() {
        if (!isEditMode) return;

        Calendar calendar = Calendar.getInstance();

        // Parse ngày hiện tại nếu có
        if (etDateOfBirth.getText().toString().trim().length() > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(etDateOfBirth.getText().toString()));
            } catch (Exception e) {
                // Dùng ngày hiện tại nếu parse lỗi
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDateOfBirth.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Giới hạn không chọn ngày tương lai
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveProfile() {
        if (!isEditMode) return;

        // Validate
        if (!validateForm()) {
            return;
        }

        // Show loading
        showLoading(true);

        // If avatar changed, upload first
        if (isAvatarChanged && selectedImageUri != null) {
            uploadAvatarThenSaveProfile();
        } else {
            saveProfileData();
        }
    }

    /**
     * Upload avatar first, then save profile data
     */
    private void uploadAvatarThenSaveProfile() {
        if (selectedImageUri == null) {
            saveProfileData();
            return;
        }

        try {
            // Create multipart using ImageUploadHelper
            MultipartBody.Part imagePart = ImageUploadHelper.createImagePart(this, selectedImageUri, "image");

            if (imagePart == null) {
                showError("Không thể tạo file upload");
                showLoading(false);
                return;
            }

            // Create other parts
            RequestBody userIdPart = ImageUploadHelper.createPartFromString(currentUser.get_id());
            RequestBody typePart = ImageUploadHelper.createPartFromString("upload_avatar");

            // Call API
            Call<UpdateProfileResponse> call = ApiService.apiService.uploadAvatar(userIdPart, typePart,imagePart);

            call.enqueue(new Callback<UpdateProfileResponse>() {
                @Override
                public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UpdateProfileResponse uploadResponse = response.body();
                        Log.d("UserProfile", "Upload response success: " + uploadResponse.isSuccess());

                        if (uploadResponse.isSuccess()) {
                            // Update avatar URL from response
                            if (uploadResponse.getUserInfo() != null && uploadResponse.getUserInfo().getAvatarUrl() != null) {
                                avatarUrl = uploadResponse.getUserInfo().getAvatarUrl();
                                Log.d("UserProfile", "Avatar uploaded, now saving profile data");
                            }
                            // Continue with profile data save
                            saveProfileData();
                        } else {
                            showLoading(false);
                            Log.e("UserProfile", "Upload failed: " + uploadResponse.getError());
                            showError("Lỗi upload ảnh: " + uploadResponse.getError());
                        }
                    } else {
                        showLoading(false);
                        // Log the error response body for debugging
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e("UserProfile", "Upload failed - Code: " + response.code() + ", Error: " + errorBody);
                        } catch (Exception e) {
                            Log.e("UserProfile", "Error reading error body: " + e.getMessage());
                        }
                        showError("Không thể upload ảnh đại diện");
                    }
                }

                @Override
                public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                    showLoading(false);
                    showError("Lỗi kết nối khi upload ảnh: " + t.getMessage());
                    Log.e("UserProfile", "Avatar upload failed", t);
                }
            });

        } catch (Exception e) {
            showLoading(false);
            showError("Lỗi khi chuẩn bị upload ảnh: " + e.getMessage());
            Log.e("UserProfile", "Error preparing avatar upload", e);
        }
    }

    /**
     * Save profile data to server
     */
    // Sửa phương thức saveProfileData()
    private void saveProfileData() {
        // ✅ Lấy username từ currentUser thay vì TextView
        String username = currentUser.getUsername();
        String displayName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String gender = getSelectedGender();

        // Tạo request
        UpdateProfileRequest request = new UpdateProfileRequest(
                currentUser.get_id(),
                username,
                displayName,
                phoneNumber,
                address,
                dateOfBirth,
                gender,
                avatarUrl
        );

        // Log để debug
        Log.d("UserProfile", "Saving profile data: " + request.toString());

        // Gọi API
        Call<UpdateProfileResponse> call = ApiService.apiService.updateProfile(request);

        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                // Reset loading state
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    UpdateProfileResponse updateResponse = response.body();
                    Log.d("UserProfile", "Update response: " + updateResponse.toString()); // ✅ Thêm log

                    if (updateResponse.isSuccess()) {
                        // Cập nhật local storage
                        updateLocalUserData(updateResponse);

                        Toast.makeText(UserProfileActivity.this,
                                "✅ Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();

                        // Reset state
                        isAvatarChanged = false;
                        selectedImageUri = null;

                        // Chuyển về View Mode
                        setViewMode();

                        // Set result
                        setResult(RESULT_OK);
                    } else {
                        String errorMsg = updateResponse.getError();
                        Log.e("UserProfile", "Update failed: " + errorMsg); // ✅ Thêm log
                        showError("Lỗi: " + errorMsg);
                    }
                } else {
                    // ✅ Thêm log chi tiết lỗi response
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("UserProfile", "Response error - Code: " + response.code() + ", Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("UserProfile", "Error reading error body: " + e.getMessage());
                    }
                    showError("Không thể cập nhật thông tin - Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                // Reset loading state
                showLoading(false);

                Log.e("UserProfile", "Network error updating profile", t); // ✅ Thêm log
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ✅ Thêm phương thức debug để kiểm tra dữ liệu
    private void debugProfileData() {
        Log.d("UserProfile", "=== DEBUG PROFILE DATA ===");
        Log.d("UserProfile", "User ID: " + currentUser.get_id());
        Log.d("UserProfile", "Username: " + currentUser.getUsername());
        Log.d("UserProfile", "Full Name: " + etFullName.getText().toString());
        Log.d("UserProfile", "Phone: " + etPhoneNumber.getText().toString());
        Log.d("UserProfile", "Address: " + etAddress.getText().toString());
        Log.d("UserProfile", "DOB: " + etDateOfBirth.getText().toString());
        Log.d("UserProfile", "Gender: " + getSelectedGender());
        Log.d("UserProfile", "Avatar URL: " + avatarUrl);
        Log.d("UserProfile", "Avatar Changed: " + isAvatarChanged);
        Log.d("UserProfile", "========================");
    }

    private boolean validateForm() {
        String displayName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();

        // Validate display name
        if (displayName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }

        if (displayName.length() < 2) {
            etFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            etFullName.requestFocus();
            return false;
        }

        // Validate phone number if provided
        if (!phoneNumber.isEmpty()) {
            if (!isValidPhoneNumber(phoneNumber)) {
                etPhoneNumber.setError("Số điện thoại không hợp lệ");
                etPhoneNumber.requestFocus();
                return false;
            }
        }

        // Validate date of birth format if provided
        if (!dateOfBirth.isEmpty()) {
            if (!isValidDateFormat(dateOfBirth)) {
                etDateOfBirth.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy)");
                etDateOfBirth.requestFocus();
                return false;
            }
        }

        // Validate gender selection
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Vietnamese phone number pattern
        String phonePattern = "^(\\+84|84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-6|8|9]|9[0-4|6-9])[0-9]{7}$";
        return phoneNumber.matches(phonePattern);
    }

    private boolean isValidDateFormat(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == rbMale.getId()) {
            return "male";
        } else if (selectedId == rbFemale.getId()) {
            return "female";
        } else if (selectedId == rbOther.getId()) {
            return "other";
        }
        return null;
    }

    private void updateLocalUserData(UpdateProfileResponse updatedUser) {
        if (updatedUser != null) {
            // Update current user object
            currentUser.setFullName(updatedUser.getUserInfo().getFullName());
            currentUser.setPhoneNumber(updatedUser.getUserInfo().getPhoneNumber());
            currentUser.setAddress(updatedUser.getUserInfo().getAddress());
            currentUser.setDateOfBirth(updatedUser.getUserInfo().getDateOfBirth());
            currentUser.setGender(updatedUser.getUserInfo().getGender());
            if (updatedUser.getUserInfo().getAvatarUrl() != null) {
                currentUser.setAvatarUrl(updatedUser.getUserInfo().getAvatarUrl());
                avatarUrl = updatedUser.getUserInfo().getAvatarUrl();
            }

            // Save to preferences
            preferenceManager.updateNormalUser(currentUser);

            // Refresh UI
            loadUserData();
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔐 Đổi mật khẩu");

        // Create layout for password change
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etCurrentPassword = new EditText(this);
        etCurrentPassword.setHint("Mật khẩu hiện tại");
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPassword);

        final EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        final EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPassword);

        builder.setView(layout);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                changePassword(currentPassword, newPassword);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            showError("Vui lòng nhập mật khẩu hiện tại");
            return false;
        }

        if (newPassword.isEmpty()) {
            showError("Vui lòng nhập mật khẩu mới");
            return false;
        }

        if (newPassword.length() < 6) {
            showError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Xác nhận mật khẩu không khớp");
            return false;
        }

        if (currentPassword.equals(newPassword)) {
            showError("Mật khẩu mới phải khác mật khẩu hiện tại");
            return false;
        }

        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        showLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentUser.getEmail(),
                currentPassword,
                newPassword
        );

        Call<ChangePasswordResponse> call = ApiService.apiService.changePassword(request);

        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ChangePasswordResponse changePasswordResponse = response.body();
                    if (changePasswordResponse.isSuccess()) {
                        Toast.makeText(UserProfileActivity.this,
                                "✅ Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        showError("Lỗi: " + changePasswordResponse.getError());
                    }
                } else {
                    Log.d(TAG, "onResponse: "+ response.body());
                    showError("Không thể đổi mật khẩu");
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e("UserProfile", "Error changing password", t);
            }
        });
    }

    private void showAvatarPreview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ảnh đại diện");

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Glide.with(this)
                .load(avatarUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.avatarshipper)
                        .error(R.drawable.shippersetting))
                .into(imageView);

        builder.setView(imageView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set dialog size
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            // Disable all interactive elements
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            btnEdit.setEnabled(false);
            btnChangePassword.setEnabled(false);
            ivCameraIcon.setEnabled(false);

            // Show loading text
            btnSave.setText("Đang lưu...");
        } else {
            // Re-enable all interactive elements
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
            btnEdit.setEnabled(true);
            btnChangePassword.setEnabled(true);
            ivCameraIcon.setEnabled(true);

            // Reset button text
            btnSave.setText("Lưu");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
        Log.e("UserProfile", message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            // Show confirmation dialog when in edit mode
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có muốn hủy các thay đổi và thoát?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        cancelEdit();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}