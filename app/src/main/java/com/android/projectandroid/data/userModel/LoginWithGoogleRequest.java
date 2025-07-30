package com.android.projectandroid.data.userModel;

public class LoginWithGoogleRequest {
    private String idToken;

    public LoginWithGoogleRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
