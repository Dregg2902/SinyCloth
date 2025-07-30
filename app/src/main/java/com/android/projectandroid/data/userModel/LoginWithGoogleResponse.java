package com.android.projectandroid.data.userModel;

public class LoginWithGoogleResponse {
    private Boolean success;
    private String type;
    private UserSignInWithGoogle userInfo;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public UserSignInWithGoogle getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserSignInWithGoogle userInfo) {
        this.userInfo = userInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
