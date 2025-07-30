package com.android.projectandroid.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.ApiService;
import com.android.projectandroid.data.userModel.Back;
import com.android.projectandroid.data.userModel.ForgotPasswordResponse;
import com.android.projectandroid.data.userModel.VerificationRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaOTPMatKhau extends AppCompatActivity {

    EditText num1, num2, num3, num4;
    Button continue_btn;
    ImageButton back;
    String email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ma_otpmat_khau);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = getIntent().getStringExtra("email");

        num1 = findViewById(R.id.otp1);
        num2 = findViewById(R.id.otp2);
        num3 = findViewById(R.id.otp3);
        num4 = findViewById(R.id.otp4);
        continue_btn = findViewById(R.id.btnLogin);
        back = findViewById(R.id.btnBack);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    if (num1.isFocused()) {
                        num2.requestFocus();
                    } else if (num2.isFocused()) {
                        num3.requestFocus();
                    } else if (num3.isFocused()) {
                        num4.requestFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        num1.addTextChangedListener(textWatcher);
        num2.addTextChangedListener(textWatcher);
        num3.addTextChangedListener(textWatcher);
        num4.addTextChangedListener(textWatcher);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back.Back_Pressed(MaOTPMatKhau.this, QuenMatKhau.class);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
                finish();
            }
        });

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = num1.getText().toString() + num2.getText().toString() + num3.getText().toString() + num4.getText().toString();
                ApiService.apiService.verify(new VerificationRequest(email, code)).enqueue(new Callback<ForgotPasswordResponse>() {
                    @Override
                    public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                        if (response.code() == 201){
                            Intent intent = new Intent(MaOTPMatKhau.this, TaoLaiMatKhau.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            overridePendingTransition(R.anim.animation1, R.anim.animation1);
                            finish();
                        }
                        else {
                            ForgotPasswordResponse forgotPasswordResponse = response.body();
                            Toast.makeText(MaOTPMatKhau.this,forgotPasswordResponse.getErr() , Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ForgotPasswordResponse> call, Throwable throwable) {

                    }
                });
            }
        });
    }
}