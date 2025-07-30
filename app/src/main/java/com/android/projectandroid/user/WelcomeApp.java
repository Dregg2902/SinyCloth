package com.android.projectandroid.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.shipper.BottomNavigationShipper;
import com.android.projectandroid.shipper.LoginAdmin;

import java.util.Objects;

public class WelcomeApp extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_app);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);

        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            if (Objects.equals(preferenceManager.getUserType(), "shipper")){
                // User is already logged in, redirect to main activity
                Intent intent = new Intent(WelcomeApp.this, BottomNavigationShipper.class);
                startActivity(intent);
                finish();
                return;
            }
            // User is already logged in, redirect to main activity
            Intent intent = new Intent(WelcomeApp.this, BottomNavigation.class);
            startActivity(intent);
            finish();
            return;
        }
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView shipperLogin = findViewById(R.id.shipperLogin);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeApp.this, LoginUser.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeApp.this, DangKyNguoiDung.class);
            startActivity(intent);
        });

        shipperLogin.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeApp.this, LoginAdmin.class);
            startActivity(intent);
        });
    }
}