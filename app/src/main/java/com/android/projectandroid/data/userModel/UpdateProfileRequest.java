// UpdateProfileRequest.java - Phiên bản đã sửa
package com.android.projectandroid.data.userModel;

public class UpdateProfileRequest {
    private String type = "update_profile";
    private String _id;
    private String username;
    private String fullName;      // ✅ THÊM TRƯỜNG FULLNAME
    private String phoneNumber;
    private String address;
    private String dateOfBirth;
    private String gender;
    private String avatarUrl;

    // ✅ SỬA: Constructor cần bao gồm fullName
    public UpdateProfileRequest(String userId, String username, String fullName,
                                String phoneNumber, String address, String dateOfBirth,
                                String gender, String avatarUrl) {
        this._id = userId;
        this.username = username;
        this.fullName = fullName;     // ✅ THÊM DÒNG NÀY
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
    }

    // Constructor rỗng cho Gson/Jackson
    public UpdateProfileRequest() {}

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }  // ✅ SỬA: Loại bỏ dấu *

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}