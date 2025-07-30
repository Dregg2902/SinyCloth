package com.android.projectandroid.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.projectandroid.R;
import com.android.projectandroid.fragment.DonationActivity;
import com.android.projectandroid.fragment.MainActivity;
import com.android.projectandroid.fragment.PassDo;
import com.android.projectandroid.fragment.SettingUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigation extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private Fragment MainFragment;
    private Fragment DonationFragment;
    private Fragment profileFragment;
    private Fragment passFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_user);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Remove background for BottomNavigationView
        bottomNavigationView.setBackground(null);

        // Initialize fragments
        MainFragment = new MainActivity();
        DonationFragment = new DonationActivity();
        profileFragment = new SettingUser();
        passFragment = new PassDo();

        // Set the default fragment to Dashboard
        setFragment(MainFragment);

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                setFragment(MainFragment);
                return true;
            } else if (item.getItemId() == R.id.quyen_gop_do) {
                setFragment(DonationFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_setting) {
                setFragment(profileFragment);
                return true;
            }
            else if (item.getItemId() == R.id.nav_shop) {
                setFragment(passFragment);
                return true;
            }
            return false;
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent); // Handle any new intent received
    }

    private void handleIntent(Intent intent) {
        // Ưu tiên xử lý targetFragment nếu có
        String target = intent.getStringExtra("targetFragment");
        if (target != null) {
            switch (target) {
                case "profile":
                    setFragment(profileFragment);
                    bottomNavigationView.setSelectedItemId(R.id.nav_setting);
                    return;
                case "main":
                    setFragment(MainFragment);
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    return;
                case "pass":
                    setFragment(passFragment);
                    bottomNavigationView.setSelectedItemId(R.id.nav_shop);
                    return;
                case "notice":
                    setFragment(DonationFragment);
                    bottomNavigationView.setSelectedItemId(R.id.quyen_gop_do);
                    return;
            }
        }
    }

        private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}

