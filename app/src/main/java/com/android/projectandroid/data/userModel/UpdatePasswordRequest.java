package com.android.projectandroid.data.userModel;

public class UpdatePasswordRequest extends ForgotPasswordRequest{

    private String type;
    private String currentPassword;
    private String newPassword;

    public UpdatePasswordRequest(String email, String type, String newPassword, String currentPassword) {
        super(email);
        this.type = type;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
