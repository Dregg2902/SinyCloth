package com.android.projectandroid.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.Back;
import com.android.projectandroid.data.userModel.NormalLoginResponse;
import com.android.projectandroid.data.userModel.SignUpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangKyNguoiDung extends AppCompatActivity {
    EditText username, email, password, confirm_password;
    TextView username_error, email_error, password_error, confirm_password_error ;
    Button signup;
    ImageButton back, showpass, showconfirm;

    ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_ky_nguoi_dung);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back = findViewById(R.id.btnBack);
        back.setOnClickListener(view -> {
            Back.Back_Pressed(DangKyNguoiDung.this, WelcomeApp.class);
            overridePendingTransition(R.anim.animation3, R.anim.animation4);
            finish();
        });

        username = findViewById(R.id.edtHoten);
        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtMatKhauMoi);
        confirm_password = findViewById(R.id.edtXNMatKhauMoi);
        showpass = findViewById(R.id.showpass);
        showconfirm = findViewById(R.id.showpassconfirm);
        username_error = findViewById(R.id.username_error3);
        email_error = findViewById(R.id.email_error);
        password_error = findViewById(R.id.password_error3);
        confirm_password_error = findViewById(R.id.confirm_password_error3);
        signup = findViewById(R.id.btnLoginSucces);



        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString();
                String mail = email.getText().toString();
                String pass = password.getText().toString();
                String confirm_pass = confirm_password.getText().toString();

                if (validateFields(user, mail, pass, confirm_pass)) {
                    if (validatePassword(pass, confirm_pass)) {
                        SignUpRequest signUpRequest = new SignUpRequest(user, mail, pass);
                        ApiService.apiService.sign_up(signUpRequest).enqueue(new Callback<NormalLoginResponse>() {
                            @Override
                            public void onResponse(Call<NormalLoginResponse> call, Response<NormalLoginResponse> response) {
                                if (response.code() == 201) {
                                    Toast.makeText(DangKyNguoiDung.this, "Signup Completed ", Toast.LENGTH_SHORT).show();
                                    NormalLoginResponse normalLoginResponse = response.body();
                                    Intent intent = new Intent(DangKyNguoiDung.this, WelcomeApp.class);
                                    startActivity(intent);
                                } else if (response.code() == 500) {
                                    Toast.makeText(DangKyNguoiDung.this, "Server error ", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 400) {
                                    Toast.makeText(DangKyNguoiDung.this, "'Missing required component", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<NormalLoginResponse> call, Throwable throwable) {
                                Toast.makeText(DangKyNguoiDung.this, "Error", Toast.LENGTH_SHORT).show();
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


    private boolean validateFields(String user, String mail, String pass, String confirm_pass) {
        boolean isValid = true;

        if (user.isEmpty()) {
            username_error.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            username_error.setVisibility(View.INVISIBLE);
        }

        if (mail.isEmpty()) {
            email_error.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            email_error.setVisibility(View.INVISIBLE);
        }

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
            confirm_password_error.setText("Mật khẩu không trùng khớp");
            isValid = false;
        } else {
            confirm_password_error.setVisibility(View.INVISIBLE);
        }
        return isValid;
    }

}