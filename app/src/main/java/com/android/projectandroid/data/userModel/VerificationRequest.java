package com.android.projectandroid.data.userModel;

public class VerificationRequest extends ForgotPasswordRequest{
    private String otp;

    public VerificationRequest(String email, String otp) {
        super(email);
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
