package com.android.projectandroid.data.userModel;

public class ForgotPasswordRequest {
    protected String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
