package com.android.projectandroid.data.userModel;

import android.content.Context;
import android.content.Intent;

public class Back {
    public static void Back_Pressed(Context context, Class<?> targetActivity) {
        Intent intent = new Intent(context, targetActivity);
        context.startActivity(intent);
    }
}
