package com.android.projectandroid.data.userModel;

public class UpdateProfileResponse {
    private boolean success;
    private String error;
    private String type;
    private UserInfo userInfo;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }

    public static class UserInfo {
        private String _id;
        private String username;
        private String email;
        private String fullName;    // ✅ THÊM TRƯỜNG FULLNAME
        private String dateOfBirth;
        private String phoneNumber;
        private String address;
        private String gender;
        private int points;
        private String avatarUrl;

        // Getters and Setters
        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}