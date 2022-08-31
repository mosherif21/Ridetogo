package com.example.ridetogo;

import android.content.Context;
import android.widget.Toast;

public class JToast {
    protected static int LENGTH_LONG = Toast.LENGTH_LONG;
    protected static int LENGTH_SHORT = Toast.LENGTH_SHORT;
    //toast class to only use one toast variable instead of adding many toasts to system queue which can't be cleared
    private static Toast toast;

    protected static Toast makeText(Context context, String text, int duration) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(context, text, duration);
        return toast;
    }

    protected void show() {
        toast.show();
    }
}