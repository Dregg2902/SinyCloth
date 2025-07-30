package com.android.projectandroid.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.android.projectandroid.data.userModel.ForgotPasswordRequest;
import com.android.projectandroid.data.userModel.ForgotPasswordResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuenMatKhau extends AppCompatActivity {

    EditText email;
    Button continue_btn;
    ImageButton back;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quen_mat_khau);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email = findViewById(R.id.edtEmail);
        continue_btn = findViewById(R.id.btnLogin);
//        email_error = findViewById(R.id.email_error);
        back = findViewById(R.id.btnBack);
        dialog= new Dialog(QuenMatKhau.this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(null);
        ImageView dialogicon = dialog.findViewById(R.id.icon);
        TextView dialogtitle = dialog.findViewById(R.id.title);
        TextView dialogdecript = dialog.findViewById(R.id.descript);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back.Back_Pressed(QuenMatKhau.this, LoginUser.class);
                overridePendingTransition(R.anim.animation3, R.anim.animation4);
                finish();
            }
        });
        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = email.getText().toString();
                // Check if the email is valid (LongLe)
                if (mail.contains("@") && mail.contains(".") && mail.contains("com")) {
                    // Send email to the user
                    ApiService.apiService.forgot_password(new ForgotPasswordRequest(mail))
                            .enqueue(new Callback<ForgotPasswordResponse>() {
                                @Override
                                public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                                    if (response.code() == 200){
                                        Intent intent = new Intent(QuenMatKhau.this, MaOTPMatKhau.class);
                                        intent.putExtra("email", mail);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.animation1, R.anim.animation2);
                                        finish();
                                    }
                                    else {
                                        ForgotPasswordResponse forgotPasswordResponse = response.body();
                                        Toast.makeText(QuenMatKhau.this,forgotPasswordResponse.getErr() , Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ForgotPasswordResponse> call, Throwable throwable) {
                                    dialogicon.setImageResource(R.drawable.warnning_red_2);
                                    dialogtitle.setText("Error");
                                    dialogdecript.setText("Call API error");
                                    dialog.show();
                                }
                            });
                }
//                else {
//                    if (mail.isEmpty()) {
//                        email_error.setVisibility(View.VISIBLE);
//                    }
//                    else {
//                        email_error.setVisibility(View.INVISIBLE);
//                    }
//
//                }
            }
        });
    }
}