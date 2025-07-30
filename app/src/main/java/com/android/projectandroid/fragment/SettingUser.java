package com.android.projectandroid.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.user.DonationHistoryActivity;
import com.android.projectandroid.user.LoginUser;
import com.android.projectandroid.user.MyOrdersActivity;
import com.android.projectandroid.user.UserProfileActivity;

public class SettingUser extends Fragment {

    LinearLayout btnlogout, lichsu , donhang, quyengop, hoso;
    private PreferenceManager preferenceManager;

    private NormalUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(getActivity());

        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(getContext());
    }

    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting_user, container, false);

        btnlogout = view.findViewById(R.id.btnlogout);
        lichsu = view.findViewById(R.id.lichsu);
        donhang = view.findViewById(R.id.don_hang_user);
        hoso = view.findViewById(R.id.ho_so);
        currentUser = preferenceManager.getCurrentUser();

        lichsu.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DonationHistoryActivity.class);
            intent.putExtra("userId", currentUser.get_id());
            startActivity(intent);
        });

        btnlogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
        donhang.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyOrdersActivity.class);
            startActivity(intent);
        });
        hoso.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            performLogout();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performLogout() {
        try {
            // Clear all user data from SharedPreferences
            preferenceManager.clearUserData();

            // Show logout success message
            Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Navigate to LoginUser activity
            Intent intent = new Intent(getContext(), LoginUser.class);
            // Clear all previous activities from stack
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finish current activity if it's an activity
            if (getActivity() != null) {
                getActivity().finish();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Có lỗi xảy ra khi đăng xuất", Toast.LENGTH_SHORT).show();
        }
    }
}