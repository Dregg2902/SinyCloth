package com.android.projectandroid.shipper;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.projectandroid.R;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationShipper extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final Fragment dashBoardShipper = new DashBoardShipper();
    private final Fragment notificationShipper = new NotificationShipper();
    private final Fragment mapShipper = new MapShipper();
    private final Fragment profileShipper = new ProfileShipper();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bottom_navigation_shipper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // Mặc định hiển thị Dashboard
        setFragment(dashBoardShipper);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.dashboard) {
                setFragment(dashBoardShipper);
                return true;
            } else if (item.getItemId() == R.id.notification) {
                setFragment(notificationShipper);
                return true;
            } else if (item.getItemId() == R.id.map) {
                setFragment(mapShipper);
                return true;
            }
            else if (item.getItemId() == R.id.shippersetting) {
                setFragment(profileShipper);
                return true;
            }
            return false;
        });
    }


    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}