package com.android.projectandroid.shipper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.android.projectandroid.data.userModel.LoginWithGoogleRequest;
import com.android.projectandroid.data.userModel.LoginWithGoogleResponse;
import com.android.projectandroid.data.userModel.NormalLoginRequest;
import com.android.projectandroid.data.userModel.NormalLoginResponse;
import com.android.projectandroid.data.userModel.Back;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.user.BottomNavigation;
import com.android.projectandroid.user.LoginUser;
import com.android.projectandroid.user.QuenMatKhau;
import com.android.projectandroid.user.WelcomeApp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginAdmin extends AppCompatActivity {

    EditText username,password;
    TextView forgotpass_text,username_error,password_error;
    ImageButton back;
    Button signin;

    private ProgressBar progressBar;
    Dialog errorDialog;
    static String google_name;

    ImageButton showpass;
    private PreferenceManager preferenceManager;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        preferenceManager = new PreferenceManager(this);
        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            // User is already logged in, redirect to main activity
            Intent intent = new Intent(LoginAdmin.this, BottomNavigationShipper.class);
            startActivity(intent);
            finish();
            return;
        }

        errorDialog= new Dialog(LoginAdmin.this);
        errorDialog.setContentView(R.layout.dialog);
        errorDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errorDialog.getWindow().setBackgroundDrawable(null);
        ImageView dialogicon = errorDialog.findViewById(R.id.icon);
        TextView dialogtitle = errorDialog.findViewById(R.id.title);
        TextView dialogdecript = errorDialog.findViewById(R.id.descript);

        setContentView(R.layout.activity_login_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        username = findViewById(R.id.edtEmailshipper);
        password = findViewById(R.id.edtMatKhauMoi1);
        signin = findViewById(R.id.btnLoginShipper);
        showpass = findViewById(R.id.showpass);
        progressBar = findViewById(R.id.progress_bar);
        username_error = findViewById(R.id.username_error);
        password_error = findViewById(R.id.password_error);
        forgotpass_text = findViewById(R.id.tvForgot);
        back = findViewById(R.id.btnBack);

        forgotpass_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginAdmin.this, QuenMatKhau.class);
                startActivity(intent);
                overridePendingTransition(R.anim.animation1, R.anim.animation2);
                finish();
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String pass = password.getText().toString();

                if (!user.isEmpty() && !pass.isEmpty()) {
                    NormalLoginRequest normalLoginRequest = new NormalLoginRequest(user, pass);
                    ApiService.apiService.login(normalLoginRequest).enqueue(new Callback<NormalLoginResponse>() {
                        @Override
                        public void onResponse(Call<NormalLoginResponse> call, Response<NormalLoginResponse> response) {
                            if (response.code() == 200 && response.body() != null) {
                                NormalLoginResponse res = response.body();

                                // Kiểm tra type có phải shipper không
                                if ("shipper".equalsIgnoreCase(res.getType())) {

                                    handleSuccessfulLogin(response);
                                } else {
                                    // Nếu không phải shipper
                                    dialogicon.setImageResource(R.drawable.warnning_red_2);
                                    dialogtitle.setText("Error");
                                    dialogdecript.setText("Sai tài khoản hoặc mật khẩu");
                                    errorDialog.show();
                                }
                            } else if (response.code() == 404) {
                                dialogicon.setImageResource(R.drawable.warnning_red_2);
                                dialogtitle.setText("Error");
                                dialogdecript.setText("Sai tài khoản hoặc mật khẩu");
                                errorDialog.show();
                            } else if (response.code() == 500) {
                                dialogicon.setImageResource(R.drawable.warnning_red_2);
                                dialogtitle.setText("Error");
                                dialogdecript.setText("Lỗi máy chủ");
                                errorDialog.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<NormalLoginResponse> call, Throwable throwable) {
                            dialogicon.setImageResource(R.drawable.warnning_red_2);
                            dialogtitle.setText("Error");
                            dialogdecript.setText("Login failed");
                            errorDialog.show();
                        }
                    });
                }
                else {
                    if (user.isEmpty()) {
                        username_error.setVisibility(View.VISIBLE);
                    } else {
                        username_error.setVisibility(View.INVISIBLE);
                    }
                    if (pass.isEmpty()) {
                        password_error.setVisibility(View.VISIBLE);
                    } else {
                        password_error.setVisibility(View.INVISIBLE);
                    }

                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back.Back_Pressed(LoginAdmin.this, WelcomeApp.class);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
                finish();
            }
        });
        showpass.setOnClickListener(view -> togglePasswordVisibility(password, showpass));
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
            Intent intent = new Intent(LoginAdmin.this, BottomNavigationShipper.class);
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
    private void togglePasswordVisibility(EditText editText, ImageButton toggleButton) {
        if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            // Hiện mật khẩu
            editText.setTransformationMethod(null);
            toggleButton.setImageResource(R.drawable.hien_matkhau); // icon con mắt gạch
        } else {
            // Ẩn mật khẩu
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.an_matkhau); // icon con mắt mở
        }

        // Đảm bảo con trỏ vẫn ở cuối
        editText.setSelection(editText.length());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                ApiService.apiService.google_sign_in(new LoginWithGoogleRequest(account.getIdToken())).enqueue(new Callback<LoginWithGoogleResponse>() {
                    @Override
                    public void onResponse(Call<LoginWithGoogleResponse> call, Response<LoginWithGoogleResponse> response) {
                        LoginWithGoogleResponse res = response.body();
                        Log.e("Data", res.getUserInfo().getDateOfBirth() + " " + res.getUserInfo().getPhoneNumber());
                        if (res != null){
                            google_name = res.getUserInfo().getUsername();
                            Intent intent = new Intent(LoginAdmin.this, BottomNavigationShipper.class);
                            intent.putExtra("type", res.getType());
                            intent.putExtra("token", response.headers().get("Authorization"));
                            intent.putExtra("user_data", response.body().getUserInfo());

                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                            startActivity(intent);
                            overridePendingTransition(R.anim.animation1, R.anim.animation2);
                            progressBar.setVisibility(View.GONE);
                        }
                        else {
                            Toast.makeText(LoginAdmin.this, "Sign in failed" , Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginWithGoogleResponse> call, Throwable throwable) {
                        Toast.makeText(LoginAdmin.this, "Call api error", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
            catch (ApiException e){
                progressBar.setVisibility(View.GONE);
            }
        }
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