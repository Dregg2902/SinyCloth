package com.android.projectandroid.shipper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.projectandroid.R;
import com.android.projectandroid.data.userModel.NormalUser;
import com.android.projectandroid.data.userModel.PreferenceManager;
import com.android.projectandroid.shipper.LoginAdmin;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileShipper#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileShipper extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    LinearLayout btnlogout;

    private ImageView ivAvatar;
    TextView fullname,gender,datebirth,email,sdt,diachi;
    private PreferenceManager preferenceManager;
    private NormalUser currentUser;

    public ProfileShipper() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileShipper.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileShipper newInstance(String param1, String param2) {
        ProfileShipper fragment = new ProfileShipper();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        preferenceManager = new PreferenceManager(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_shipper, container, false);

        currentUser = preferenceManager.getCurrentUser();
        ivAvatar = view.findViewById(R.id.img_avatar);

        fullname = view.findViewById(R.id.txt_fullname);
        gender = view.findViewById(R.id.txt_gender);
        datebirth = view.findViewById(R.id.txt_dob);
        email = view.findViewById(R.id.txt_email);
        sdt = view.findViewById(R.id.txt_phone);
        diachi = view.findViewById(R.id.txt_address);

        fullname.setText(currentUser.getFullName());
        gender.setText(getGenderDisplayText(currentUser.getGender()));
        datebirth.setText(currentUser.getDateOfBirth());
        email.setText(currentUser.getEmail());
        sdt.setText(currentUser.getPhoneNumber());
        diachi.setText(currentUser.getAddress());

        loadUserData();

        btnlogout = view.findViewById(R.id.btnlogout);

        btnlogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
        return view;
    }

    private String getGenderDisplayText(String gender) {
        if (gender == null) return "Chưa cập nhật";

        switch (gender.toLowerCase()) {
            case "male":
                return "Nam";
            case "female":
                return "Nữ";
            case "other":
                return "Khác";
            default:
                return "Chưa cập nhật";
        }
    }

    private void loadAvatar() {
        Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.avatarshipper)
                        .error(R.drawable.shippersetting)
                        .circleCrop())
                .into(ivAvatar);
    }
    private void loadUserData() {
        preferenceManager = new PreferenceManager(getContext());
        currentUser = preferenceManager.getCurrentUser();

        if (currentUser == null) {
            showError("Không tìm thấy thông tin người dùng");
            Intent intent = new Intent(getContext(), LoginAdmin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finishAffinity(); // kết thúc cả activity chứa fragment
        }
        loadAvatar();
    }
    private void showError(String message) {
        Toast.makeText(getContext(), "❌ " + message, Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(getContext(), LoginAdmin.class);
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