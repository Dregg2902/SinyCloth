package com.android.projectandroid.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.Back;
import com.android.projectandroid.data.userModel.ForgotPasswordResponse;
import com.android.projectandroid.data.userModel.SessionManager;
import com.android.projectandroid.data.userModel.UpdatePasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaoLaiMatKhau extends AppCompatActivity {
    EditText password, confirm_password;
    Button reset_password;
    ImageButton back,showpass, showconfirm;
    TextView password_error, confirm_password_error ;
    String email;
    Dialog successdialog;

    private boolean isAtleast8 = false, hasUppercase = false, hasNumber = false, hasSymbol = false, isReset = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tao_lai_mat_khau);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main5), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = getIntent().getStringExtra("email");

        password = findViewById(R.id.edtMatKhauMoi);
        confirm_password = findViewById(R.id.edtXNMatKhauMoi);
        password_error = findViewById(R.id.password_error);
        confirm_password_error = findViewById(R.id.confirm_password_error);
        showpass = findViewById(R.id.showpass);
        showconfirm = findViewById(R.id.showpassconfirm);
        reset_password = findViewById(R.id.btnLogin);
        back = findViewById(R.id.btnBack);
        successdialog = new Dialog(TaoLaiMatKhau.this);
        successdialog.setContentView(R.layout.dialog);
        successdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successdialog.getWindow().setBackgroundDrawable(null);
        ImageView dialogicon = successdialog.findViewById(R.id.icon);
        TextView dialogtitle = successdialog.findViewById(R.id.title);
        TextView dialogdecript = successdialog.findViewById(R.id.descript);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back.Back_Pressed(TaoLaiMatKhau.this, QuenMatKhau.class);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
                finish();
            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = password.getText().toString();
                String confirm_pass = confirm_password.getText().toString();

                if (validateFields(pass, confirm_pass)) {
                    if (validatePassword(pass, confirm_pass)) {
                        isReset = true;
                        //Call API
                        ApiService.apiService.update_password("", new UpdatePasswordRequest(email,"forgot_password", pass, "")).enqueue(new Callback<ForgotPasswordResponse>() {
                            @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
                            @Override
                            public void onResponse(@NonNull Call<ForgotPasswordResponse> call, @NonNull Response<ForgotPasswordResponse> response) {
//                                String a = response.toString();
//                                Log.e("mytag", "onResponse:" + a);
                                if (response.code() == 201){
                                    dialogicon.setImageResource(R.drawable.checked);
                                    dialogtitle.setText("Password Reset!");
                                    dialogdecript.setText("Your password has been reset successfully");
                                    successdialog.setOnShowListener(dialog -> {
                                        View view = successdialog.getWindow().getDecorView();
                                        view.setOnTouchListener((v, event) -> {
                                            successdialog.dismiss();
                                            Intent intent = new Intent(TaoLaiMatKhau.this, WelcomeApp.class);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.animation3, R.anim.animation4);
                                            finish();
                                            return true;
                                        });
                                    });
                                    successdialog.show();
                                }
                                else if (response.code() == 401){
                                    new SessionManager(TaoLaiMatKhau.this).Logout();
                                }
                                else {
                                    ForgotPasswordResponse forgotPasswordResponse = response.body();
                                    Toast.makeText(TaoLaiMatKhau.this,forgotPasswordResponse.getErr() , Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ForgotPasswordResponse> call, Throwable throwable) {

                            }
                        });
                    }
                }
            }
        });
        showpass.setOnClickListener(view -> togglePasswordVisibility(password, showpass));
        showconfirm.setOnClickListener(view -> togglePasswordVisibility(confirm_password, showconfirm));

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
    private boolean validateFields(String pass, String confirm_pass) {
        boolean isValid = true;

        if (pass.isEmpty()) {
            password_error.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            password_error.setVisibility(View.INVISIBLE);
        }

        if (confirm_pass.isEmpty()) {
            confirm_password_error.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            confirm_password_error.setVisibility(View.INVISIBLE);
        }

        return isValid;
    }

    private boolean validatePassword(String pass, String confirm_pass) {
        boolean isValid = true;
//
//        if (pass.length() >= 8) {
//            isAtleast8 = true;
//        } else {
//            isAtleast8 = false;
//            password_error.setVisibility(View.VISIBLE);
//            password_error.setText("Password must be at least 8 characters");
//            isValid = false;
//        }
//
//        if (pass.matches(".*[A-Z].*")) {
//            hasUppercase = true;
//        } else {
//            hasUppercase = false;
//            password_error.setVisibility(View.VISIBLE);
//            password_error.setText("Password must contain at least 1 uppercase letter");
//            isValid = false;
//        }
//
//        if (pass.matches(".*\\d.*")) {
//            hasNumber = true;
//        } else {
//            hasNumber = false;
//            password_error.setVisibility(View.VISIBLE);
//            password_error.setText("Password must contain at least 1 number");
//            isValid = false;
//        }
//
//        if (pass.matches(".*[!@#$%^&*].*")) {
//            hasSymbol = true;
//        } else {
//            hasSymbol = false;
//            password_error.setVisibility(View.VISIBLE);
//            password_error.setText("Password must contain at least 1 symbol");
//            isValid = false;
//        }
//
        if (!pass.equals(confirm_pass)) {
            confirm_password_error.setVisibility(View.VISIBLE);
            confirm_password_error.setText("Passwords do not match");
            isValid = false;
        } else {
            confirm_password_error.setVisibility(View.INVISIBLE);
        }

        return isValid;
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