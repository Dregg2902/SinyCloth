package com.android.projectandroid.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.Back;
import com.android.projectandroid.data.userModel.NormalLoginRequest;
import com.android.projectandroid.data.userModel.NormalLoginResponse;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.data.userModel.NormalUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginUser extends AppCompatActivity {

    EditText username, password;
    TextView forgotpass_text, username_error, password_error;
    ImageButton back;
    Button signin;
    CheckBox rememberLogin; // Thêm checkbox nhớ đăng nhập

    private ProgressBar progressBar;
    Dialog errorDialog;
    ImageButton showpass;

    // Add PreferenceManager
    private PreferenceManager preferenceManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);

        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            // User is already logged in, redirect to main activity
            Intent intent = new Intent(LoginUser.this, BottomNavigation.class);
            startActivity(intent);
            finish();
            return;
        }

        errorDialog = new Dialog(LoginUser.this);
        errorDialog.setContentView(R.layout.dialog);
        errorDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errorDialog.getWindow().setBackgroundDrawable(null);
        ImageView dialogicon = errorDialog.findViewById(R.id.icon);
        TextView dialogtitle = errorDialog.findViewById(R.id.title);
        TextView dialogdecript = errorDialog.findViewById(R.id.descript);

        setContentView(R.layout.activity_login_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners(dialogicon, dialogtitle, dialogdecript);
        loadSavedCredentials(); // Load saved username/password if remember login is enabled
    }

    private void initializeViews() {
        username = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtMatKhauMoi);
        signin = findViewById(R.id.btnLogin);
        showpass = findViewById(R.id.showpass);
        progressBar = findViewById(R.id.progress_bar);
        username_error = findViewById(R.id.username_error2);
        password_error = findViewById(R.id.password_error2);
        forgotpass_text = findViewById(R.id.tvForgot);
        back = findViewById(R.id.btnBack);
        rememberLogin = findViewById(R.id.rememberLogin); // Thêm dòng này
    }

    private void loadSavedCredentials() {
        // Load saved credentials if remember login was checked
        if (preferenceManager.isRememberLoginEnabled()) {
            String savedUsername = preferenceManager.getSavedUsername();
            String savedPassword = preferenceManager.getSavedPassword();

            if (savedUsername != null && !savedUsername.isEmpty()) {
                username.setText(savedUsername);
            }
            if (savedPassword != null && !savedPassword.isEmpty()) {
                password.setText(savedPassword);
            }
            rememberLogin.setChecked(true);
        }
    }

    private void setupClickListeners(ImageView dialogicon, TextView dialogtitle, TextView dialogdecript) {
        forgotpass_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginUser.this, QuenMatKhau.class);
                startActivity(intent);
                overridePendingTransition(R.anim.animation1, R.anim.animation2);
                finish();
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogin(dialogicon, dialogtitle, dialogdecript);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back.Back_Pressed(LoginUser.this, WelcomeApp.class);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
                finish();
            }
        });

        showpass.setOnClickListener(view -> togglePasswordVisibility(password, showpass));
    }

    private void performLogin(ImageView dialogicon, TextView dialogtitle, TextView dialogdecript) {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        // Reset error states
        username_error.setVisibility(View.INVISIBLE);
        password_error.setVisibility(View.INVISIBLE);

        // Validate input
        if (user.isEmpty() || pass.isEmpty()) {
            showValidationErrors(user.isEmpty(), pass.isEmpty());
            return;
        }

        // Show loading
        showLoading(true);

        // Create login request
        NormalLoginRequest normalLoginRequest = new NormalLoginRequest(user, pass);

        // Make API call
        ApiService.apiService.login(normalLoginRequest).enqueue(new Callback<NormalLoginResponse>() {
            @Override
            public void onResponse(Call<NormalLoginResponse> call, Response<NormalLoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    NormalLoginResponse loginResponse = response.body();

                    if (loginResponse.getSuccess() && loginResponse.getUser() != null) {
                        // Save credentials if remember login is checked
                        if (rememberLogin.isChecked()) {
                            preferenceManager.saveCredentials(user, pass);
                            preferenceManager.setRememberLogin(true);
                        } else {
                            preferenceManager.clearSavedCredentials();
                            preferenceManager.setRememberLogin(false);
                        }

                        // ✅ Truyền full response để lấy header
                        handleSuccessfulLogin(response);
                    } else {
                        showErrorDialog(dialogicon, dialogtitle, dialogdecript,
                                "Login Failed", "Invalid response from server");
                    }
                } else {
                    handleLoginError(response.code(), dialogicon, dialogtitle, dialogdecript);
                }
            }

            @Override
            public void onFailure(Call<NormalLoginResponse> call, Throwable throwable) {
                showLoading(false);
                Log.e("LoginError", "Login failed", throwable);
                showErrorDialog(dialogicon, dialogtitle, dialogdecript,
                        "Connection Error", "Please check your internet connection and try again");
            }
        });
    }

    private void handleSuccessfulLogin(Response<NormalLoginResponse> response) {
        try {
            NormalLoginResponse loginResponse = response.body();
            NormalUser user = loginResponse.getUser();
            String userType = loginResponse.getType();

            // ✅ Lấy token từ header Authorization
            String token = response.headers().get("Authorization");

            // Lưu vào SharedPreferences
            preferenceManager.saveNormalUserData(
                    user.get_id(),
                    token,
                    userType,
                    user
            );

            // Điều hướng sang BottomNavigation
            Intent intent = new Intent(LoginUser.this, BottomNavigation.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("token", token);
            intent.putExtra("type", userType);
            intent.putExtra("user_data", user);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("LoginError", "Error processing login response", e);
            Toast.makeText(this, "Error processing login. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginError(int responseCode, ImageView dialogicon,
                                  TextView dialogtitle, TextView dialogdecript) {
        switch (responseCode) {
            case 401:
            case 404:
                showErrorDialog(dialogicon, dialogtitle, dialogdecript,
                        "Login Failed", "Incorrect username or password");
                break;
            case 500:
                showErrorDialog(dialogicon, dialogtitle, dialogdecript,
                        "Server Error", "Server is temporarily unavailable. Please try again later.");
                break;
            default:
                showErrorDialog(dialogicon, dialogtitle, dialogdecript,
                        "Error", "Login failed. Please try again.");
                break;
        }
    }

    private void showValidationErrors(boolean usernameEmpty, boolean passwordEmpty) {
        if (usernameEmpty) {
            username_error.setVisibility(View.VISIBLE);
        }
        if (passwordEmpty) {
            password_error.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorDialog(ImageView dialogicon, TextView dialogtitle,
                                 TextView dialogdecript, String title, String message) {
        dialogicon.setImageResource(R.drawable.warnning_red_2);
        dialogtitle.setText(title);
        dialogdecript.setText(message);
        errorDialog.show();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            signin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            signin.setEnabled(true);
        }
    }

    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            // Show password
            editText.setTransformationMethod(null);
            toggleButton.setImageResource(R.drawable.hien_matkhau);
        } else {
            // Hide password
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.an_matkhau);
        }

        // Keep cursor at the end
        editText.setSelection(editText.length());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    hideKeyboard2(view);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard2(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}