package com.android.projectandroid.data.userModel;

public class ForgotPasswordResponse {
    private String err;
    private boolean success;

    public ForgotPasswordResponse(String err, boolean success) {
        this.err = err;
        this.success = success;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
