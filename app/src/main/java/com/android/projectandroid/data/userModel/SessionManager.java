package com.android.projectandroid.data.userModel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.projectandroid.user.LoginUser;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Logout", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public void Logout(){
        editor.clear();
        editor.apply();

        Intent intent = new Intent(context, LoginUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
